package com.intuit.socialmedia.posts.service;

import com.intuit.socialmedia.posts.dto.request.AddCommentRequest;
import com.intuit.socialmedia.posts.dto.response.CommentResponse;

import java.util.List;

public interface ICommentService {
    void addComment(String postId, AddCommentRequest request);
    List<CommentResponse> getPaginatedCommentsByPostId(String postId, int pageSize, int pageNum);
}