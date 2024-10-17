package com.intuit.socialmedia.posts.service.impl.postmanagement;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.entity.Post;
import com.intuit.socialmedia.posts.repository.PostDao;
import com.intuit.socialmedia.posts.service.impl.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PostDeletionService {
    private final PostDao postDao;
    private final RedisService redisService;

    @Autowired
    public PostDeletionService(PostDao postDao, RedisService redisService) {
        this.postDao = postDao;
        this.redisService = redisService;
    }

    public boolean deletePost(String postId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Post> postDaoById = postDao.findById(postId);
        if (postDaoById.map(p -> !p.getCreatedBy().equals(userDetails.getId())).orElse(true)) {
            throw new BadCredentialsException("User not authorized to delete "+ postDaoById.get().getId() +" post" );
        }

        postDaoById.get().setStatus(Status.DELETED);

        //mark post deleted in redis
        redisService.setValue(postId, postDaoById.get());

        //delete in DB
        postDao.save(postDaoById.get());
        return true;
    }
}
