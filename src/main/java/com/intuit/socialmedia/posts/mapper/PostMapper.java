package com.intuit.socialmedia.posts.mapper;

import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostMapper {
    Post createRequestToPostEntity(PostCreateRequest request);
    PostResponse postEntityToPostResponse(Post postEntity);

}
