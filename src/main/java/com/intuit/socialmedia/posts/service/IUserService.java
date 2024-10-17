package com.intuit.socialmedia.posts.service;

import com.intuit.socialmedia.posts.dto.request.RegisterNewUserRequest;
import com.intuit.socialmedia.posts.dto.request.UserLoginRequest;
import com.intuit.socialmedia.posts.dto.response.UserResponse;

public interface IUserService {
    void registerUser(RegisterNewUserRequest newUserRequest);

    UserResponse login(UserLoginRequest userLoginRequest) throws Exception;

    UserResponse getUser(String userId);

}
