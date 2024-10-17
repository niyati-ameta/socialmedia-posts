package com.intuit.socialmedia.posts.service;

public interface IFollowService {
    void followUser(String followUserId);

    boolean unfollowUser(String unfollowUserId);

}
