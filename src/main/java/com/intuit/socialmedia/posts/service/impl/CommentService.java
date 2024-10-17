package com.intuit.socialmedia.posts.service.impl;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.dto.request.AddCommentRequest;
import com.intuit.socialmedia.posts.dto.response.CommentResponse;
import com.intuit.socialmedia.posts.entity.Comment;
import com.intuit.socialmedia.posts.repository.CommentDao;
import com.intuit.socialmedia.posts.service.ICommentService;
import com.intuit.socialmedia.posts.service.IRedisService;
import com.intuit.socialmedia.posts.util.RedisKeyParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CommentService implements ICommentService {
    private final CommentDao commentDao;
    private final IRedisService redisService;
    private final RedisKeyParser redisKeyParser;

    @Autowired
    public CommentService(CommentDao commentDao, IRedisService redisService, RedisKeyParser redisKeyParser) {
        this.commentDao = commentDao;
        this.redisService = redisService;
        this.redisKeyParser = redisKeyParser;
    }

    @Override
    public void addComment(String postId, AddCommentRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment commentEntity = createCommentEntity(postId, request, userDetails);
        Comment comment = commentDao.save(commentEntity);
        saveCommentToRedis(postId, comment, userDetails);

        CommentResponse.builder().body(request.getBody())
                .createdByUserId(userDetails.getId()).build();
    }

    private Comment createCommentEntity(String postId, AddCommentRequest request, CustomUserDetails userDetails) {
        return Comment.builder().body(request.getBody()).status(Status.ACTIVE)
                .createdBy(userDetails.getId()).postId(postId).build();
    }

    private void saveCommentToRedis(String postId, Comment comment, CustomUserDetails userDetails) {
        redisService.addToListAndTrim(redisKeyParser.prepareKey(Map.of("postId", postId), RedisKeyParser.redisCommentsPerPost),
                CommentResponse.builder().id(comment.getId())
                        .body(comment.getBody()).createdByUserId(userDetails.getId())
                        .status(Status.ACTIVE).createdOn(comment.getCreatedOn())
                        .updatedOn(comment.getUpdatedOn()).build());
    }


    @Override
    //0 based index pageNum
    public List<CommentResponse> getPaginatedCommentsByPostId(String postId, int pageSize, int pageNumber) {
        List<Object> commentObjects = redisService.getList(redisKeyParser.prepareKey(Map.of("postId", postId), RedisKeyParser.redisCommentsPerPost));

        if(commentObjects != null && commentObjects.size() >= pageSize * pageNumber)
            return commentObjects.stream().map((comment -> (CommentResponse)comment))
                    .skip((long) pageNumber * pageSize).limit(pageSize).toList();

        return commentDao.findByPostIdAndStatus(postId, Status.ACTIVE, Pageable.ofSize(pageSize).withPage(pageNumber))
                .stream().map(comment -> CommentResponse.builder().
                        createdByUserId(comment.getCreatedBy()).body(comment.getBody()).status(Status.ACTIVE)
                        .createdOn(comment.getCreatedOn()).updatedOn(comment.getUpdatedOn()).build())
                .toList();
    }

}
