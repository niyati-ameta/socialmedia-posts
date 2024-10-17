package com.intuit.socialmedia.posts.mapper;

import com.intuit.socialmedia.posts.dto.request.RegisterNewUserRequest;
import com.intuit.socialmedia.posts.dto.response.UserAuthResponse;
import com.intuit.socialmedia.posts.dto.response.UserResponse;
import com.intuit.socialmedia.posts.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserAuthResponse userMapToUserAuthResponse(Map<String, String> userMap);
    User requestToUserEntity(RegisterNewUserRequest request);
    UserAuthResponse entityToUserAuthResponse(User user);
    UserResponse entityToUserResponse(User user);

}