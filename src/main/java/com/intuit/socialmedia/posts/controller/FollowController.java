package com.intuit.socialmedia.posts.controller;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.service.IFollowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/follow")
@Slf4j
@Tag(name = "Follow Controller", description = "CRUD APIs related to user following")
public class FollowController {

    @Autowired
    private IFollowService followService;

    @PostMapping("/{followUserId}")
    public ResponseEntity<String> followUser(@PathVariable String followUserId) {
        log.info("follow request to user {}", followUserId);
        followService.followUser(followUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully followed User " + followUserId);
    }

    @DeleteMapping("/{followUserId}")
    public ResponseEntity<String> unfollowUser(@PathVariable String unfollowUserId) {
        log.info("unfollow request to user {}", unfollowUserId);
        if (followService.unfollowUser(unfollowUserId)) {
            return ResponseEntity.ok("Successfully unfollowed User " + unfollowUserId);
        }
        return ResponseEntity.notFound().build();
    }
}
