package com.intuit.socialmedia.posts.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.entity.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {
    private String id;
    private String description;
    private List<CommentResponse> comments;
    private List<PostCreateRequest.Media> mediaList;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;
    private String createdById; // userId
    private String createdByName;
}
