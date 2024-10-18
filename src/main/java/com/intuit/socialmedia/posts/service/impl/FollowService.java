package com.intuit.socialmedia.posts.service.impl;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.entity.UserFollowMapping;
import com.intuit.socialmedia.posts.entity.UserFollowMappingId;
import com.intuit.socialmedia.posts.exception.ResourceNotFoundException;
import com.intuit.socialmedia.posts.exception.UserAlreadyExistsException;
import com.intuit.socialmedia.posts.model.RedisRecentPostObject;
import com.intuit.socialmedia.posts.repository.UserDao;
import com.intuit.socialmedia.posts.repository.UserFollowMappingDao;
import com.intuit.socialmedia.posts.service.IFollowService;
import com.intuit.socialmedia.posts.service.IRedisService;
import com.intuit.socialmedia.posts.util.RedisKeyParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FollowService implements IFollowService {

    private final UserFollowMappingDao followMappingDao;
    private final IRedisService redisService;
    private final UserDao userDao;
    private final RedisKeyParser redisKeyParser;

    @Autowired
    public FollowService(UserFollowMappingDao followMappingDao, IRedisService redisService, UserDao userDao, RedisKeyParser redisKeyParser) {
        this.followMappingDao = followMappingDao;
        this.redisService = redisService;
        this.userDao = userDao;
        this.redisKeyParser = redisKeyParser;
    }

    @Override
    public void followUser(String followUserId) {
        log.info("inside follow-user method");
        CustomUserDetails userDetails = (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Check if the user is already following the followee
        followMappingDao.findById(new UserFollowMappingId(userDetails.getId(), followUserId))
                .ifPresent(value -> { throw new UserAlreadyExistsException("You are already following this user."); });

        //can be checked in redis first
        userDao.findById(followUserId).orElseThrow(() ->  new ResourceNotFoundException("followee userId doesn't exist"));
        UserFollowMapping followMapping = new UserFollowMapping(new UserFollowMappingId(userDetails.getId(), followUserId));
        log.info("adding follow-followee mapping");
        followMappingDao.save(followMapping);
    }

    @Override
    public boolean unfollowUser(String unfollowUserId) {
        CustomUserDetails userDetails = (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Object> recentPostObjects = redisService.getList(redisKeyParser.prepareKey(Map.of("userId", userDetails.getId()), redisKeyParser.redisRecentPostsPerFollower));
        recentPostObjects = recentPostObjects.stream().filter(rp -> !((RedisRecentPostObject)rp).getCreatedByUserId().equals(unfollowUserId)).toList();
        redisService.setValue(redisKeyParser.prepareKey(Map.of("userId", userDetails.getId()), redisKeyParser.redisRecentPostsPerFollower), recentPostObjects);

        List<Object> snapPostObjects = redisService.getList(redisKeyParser.prepareKey(Map.of("userId", userDetails.getId()), redisKeyParser.redisSnapPostsPerFollower));
        recentPostObjects = snapPostObjects.stream().filter(rp -> !((RedisRecentPostObject)rp).getCreatedByUserId().equals(unfollowUserId)).toList();
        redisService.setValue(redisKeyParser.prepareKey(Map.of("userId", userDetails.getId()), redisKeyParser.redisSnapPostsPerFollower), recentPostObjects);

        Optional<UserFollowMapping> mapping = followMappingDao.findById(new UserFollowMappingId(userDetails.getId(), unfollowUserId));
        if (mapping.isPresent()) {
            // Delete the mapping if it exists
            followMappingDao.delete(mapping.get());
            return true; // Successfully unfollowed
        }

        return false; // No mapping found
    }
}
