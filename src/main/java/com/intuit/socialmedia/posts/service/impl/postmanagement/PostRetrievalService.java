package com.intuit.socialmedia.posts.service.impl.postmanagement;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.dto.request.PostFilterRequest;
import com.intuit.socialmedia.posts.dto.response.CommentResponse;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.entity.Post;
import com.intuit.socialmedia.posts.entity.User;
import com.intuit.socialmedia.posts.entity.UserFollowMapping;
import com.intuit.socialmedia.posts.model.RedisRecentPostObject;
import com.intuit.socialmedia.posts.repository.CommentDao;
import com.intuit.socialmedia.posts.repository.PostDao;
import com.intuit.socialmedia.posts.repository.UserDao;
import com.intuit.socialmedia.posts.repository.UserFollowMappingDao;
import com.intuit.socialmedia.posts.service.impl.RedisService;
import com.intuit.socialmedia.posts.util.RedisKeyParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostRetrievalService {
    private final RedisService redisService;
    private final CommentDao commentDao;
    private final UserDao userDao;
    private final PostDao postDao;
    private final UserFollowMappingDao userFollowMappingDao;
    private final RedisKeyParser redisKeyParser;


    @Autowired
    public PostRetrievalService(RedisService redisService, CommentDao commentDao, UserDao userDao, PostDao postDao, UserFollowMappingDao userFollowMappingDao, RedisKeyParser redisKeyParser) {
        this.redisService = redisService;
        this.commentDao = commentDao;
        this.userDao = userDao;
        this.postDao = postDao;
        this.userFollowMappingDao = userFollowMappingDao;
        this.redisKeyParser = redisKeyParser;
    }

    public List<PostResponse> getPostsResponse(PostFilterRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Post> posts = getPosts(request, userDetails.getId());

        List<String> redisUserIdKeys = posts.stream().map(Post::getCreatedBy).collect(Collectors.toList());
        Map<String, Object> redisValues = redisService.multiGet(redisUserIdKeys);

        Map<String, String> userIdToNameMap = new HashMap<>();
        Map<String, Object> redisUserIdToNameMap = new HashMap<>();

        List<User> userIdNames = userDao.findByIdIn(redisValues.entrySet().stream()
                .filter(entrySet -> entrySet.getValue() == null).map(Map.Entry::getKey).collect(Collectors.toList()));

        for (User user : userIdNames) {
            userIdToNameMap.put(user.getId(), user.getName());
            redisUserIdToNameMap.put(redisKeyParser.prepareKey(Map.of("userId", user.getId()), RedisKeyParser.redisUserDetails), user.getName());
        }

        //Fetch 10 recent comments for each post from redis if present
        Map<String, List<CommentResponse>> postToCommentResonseMap = preparePostToCommentResponseMap(posts);
        List<PostResponse> response = posts.stream().map((post) ->
                PostResponse.builder()
                        .id(post.getId())
                        .description(post.getDescription()).createdById(post.getCreatedBy())
                        .updatedOn(post.getUpdatedOn()).createdOn(post.getCreatedOn())
                        .comments(postToCommentResonseMap.get(post.getId())).mediaList(post.getMediaList())
                        .createdByName((StringUtils.isBlank((String) redisValues.get(post.getCreatedBy()))) ? userIdToNameMap.get(post.getCreatedBy()) : (String) redisValues.get(post.getCreatedBy()))
                        .build()).toList();

        log.info("updating redis will multiset for missing user details mapping : {}", redisUserIdToNameMap);
        redisService.multiSet(redisUserIdToNameMap);
        if(posts.isEmpty()) {
            log.info("No posts to display ...");
        }


        return response;
    }

    private Map<String, List<CommentResponse>> preparePostToCommentResponseMap(List<Post> posts) {
        log.info("preparing comments for posts {}", posts.toString());
        Map<String, List<CommentResponse>> postToCommentResonseMap = new HashMap<>();
        for (Post post : posts) {
            List<Object> commentObjects = redisService.getList(redisKeyParser.prepareKey(Map.of("postId", post.getId()), RedisKeyParser.redisCommentsPerPost));
            if (commentObjects != null) {
                log.info("preparing comment for postId: {} from redis", post.getId());
                postToCommentResonseMap.put(post.getId(), commentObjects.stream().map((comment -> (CommentResponse) comment)).limit(10).toList());
            }
            else {
                log.info("preparing comment for postId: {} from database", post.getId());
                List<CommentResponse> commentResponseList = commentDao.findByPostIdAndStatus(post.getId(), Status.ACTIVE, Pageable.ofSize(0).withPage(100))
                        .stream().map(comment -> CommentResponse.builder().
                                createdByUserId(comment.getCreatedBy()).body(comment.getBody())
                                .createdOn(comment.getCreatedOn()).build())
                        .toList();
                redisService.addList(redisKeyParser.prepareKey(Map.of("postId", post.getId()), RedisKeyParser.redisCommentsPerPost), Arrays.asList(commentResponseList.toArray()));
                postToCommentResonseMap.put(post.getId(), commentResponseList);
            }
        }
        return postToCommentResonseMap;
    }

    private List<Post> getPosts(PostFilterRequest request, String userId) {
        log.info("inside get post method with request: {}", request.toString());
        Instant instant = Instant.ofEpochSecond(request.getEpoch());
        OffsetDateTime requestedOffsetDateTime = instant.atOffset(ZoneOffset.UTC);
        //fetch from redis, cachedPosts eligible to view by this user, in between scrolls
        List<RedisRecentPostObject> recentPosts = redisService.getList(redisKeyParser.prepareKey(Map.of("userId", userId), RedisKeyParser.redisSnapPostsPerFollower))
                .stream().map(ob -> (RedisRecentPostObject) ob)
                .collect(Collectors.toList());


        if (recentPosts.isEmpty() || recentPosts.get(0).getCreatedOn().isBefore(requestedOffsetDateTime)) {
            log.info("creating snapshot for follower userId: {}", userId);
            recentPosts = redisService.getList(redisKeyParser.prepareKey(Map.of("userId", userId), RedisKeyParser.redisRecentPostsPerFollower))
                    .stream().map(ob -> (RedisRecentPostObject) ob)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            redisService.replaceList(redisKeyParser.prepareKey(Map.of("userId", userId), RedisKeyParser.redisSnapPostsPerFollower), Arrays.asList(recentPosts.toArray()));
        }

        List<String> recentPostIdsFilteredByDate = recentPosts.stream()
                .filter(rp -> rp.getCreatedOn().isBefore(requestedOffsetDateTime) || rp.getCreatedOn().isEqual(requestedOffsetDateTime))
                .map(RedisRecentPostObject::getPostId).collect(Collectors.toList());

        Map<String, Object> postMultiGetResponse = redisService.multiGet(recentPostIdsFilteredByDate);
        List<String> missingPostsInRedis = postMultiGetResponse.entrySet().stream().filter(postEntry -> postEntry.getValue() == null).map(Map.Entry::getKey).toList();

        //fetch missing posts from DB
        log.info("fetching missing postId's detail from database");
        List<Post> missingPostsFromDB = missingPostsInRedis.isEmpty() ? Collections.emptyList() : postDao.findByIdIn(missingPostsInRedis);
        Map<String, Post> missingPostMap = new HashMap<>();
        for (Post post : missingPostsFromDB) {
            missingPostMap.put(post.getId(), post);
        }

        List<Post> cachedPosts = postMultiGetResponse.entrySet().stream()
                .map(postEntry -> {
                    if (postEntry.getValue() == null)
                        return missingPostMap.get(postEntry.getKey());
                    return (Post) postEntry.getValue();
                }).filter(post -> post.getStatus().equals(Status.ACTIVE))
                .toList();

        Map<String, Object> postIdToPostMap = new HashMap<>();
        if (cachedPosts.size() >= request.getPageSize()) {
            for (Post op : missingPostsFromDB)
                postIdToPostMap.put(op.getId(), op);

            redisService.multiSet(postIdToPostMap);
            log.info("RETURNING FROM CACHE .. ");
            return cachedPosts.subList(0, request.getPageSize());
        }

        // if requested number is more than cached, get older posts from DB
        // set to make sure duplicate post ids not getting saved
        Set<String> cachedPostsIds = cachedPosts.stream().map(Post::getId).collect(Collectors.toSet());

        //get all followers of this user
        log.info("no enough posts in redis. Falling back to database");
        List<Post> oldPosts = findOlderPostsByFollowees(requestedOffsetDateTime, Pageable.ofSize(request.getPageSize() + 50), userId);
        if (!oldPosts.isEmpty()) {
            // add older Posts in redis against user
            redisService.addList(redisKeyParser.prepareKey(Map.of("userId", userId), RedisKeyParser.redisSnapPostsPerFollower),
                    oldPosts.stream()
                            .filter(op -> !cachedPostsIds.contains(op.getId()))
                            .map(oP -> new RedisRecentPostObject(oP.getId(), oP.getCreatedOn(), oP.getCreatedBy()))
                            .collect(Collectors.toList()));

            // add postId to Post mapping in redis for older Posts
            for (Post op : oldPosts) {
                postIdToPostMap.put(op.getId(), op);
            }

            log.info("updating redis for posts");
            redisService.multiSet(postIdToPostMap);
            List<Post> filteredOldPosts =  oldPosts.stream().filter(post -> post.getCreatedOn()
                    .isBefore(requestedOffsetDateTime) || post.getCreatedOn().isEqual(requestedOffsetDateTime)).collect(Collectors.toList());
            log.info("FETCHED FROM DB ..");
            return filteredOldPosts.subList(0, Math.min(request.getPageSize(), filteredOldPosts.size()));
        }

        return Collections.emptyList();
    }

    private List<Post> findOlderPostsByFollowees(OffsetDateTime requestedOffsetDateTime, Pageable pageSize, String userId) {
        List<UserFollowMapping> followMappings = userFollowMappingDao.findByIdFollower(userId);
        List<String> allFollowees = followMappings.stream().map(fm -> fm.getId().getFollowing()).collect(Collectors.toList());
        Slice<Post> oldPosts = postDao.findOlderActivePostsByUsers(requestedOffsetDateTime, allFollowees, pageSize);
        return oldPosts.getContent();
    }

}
