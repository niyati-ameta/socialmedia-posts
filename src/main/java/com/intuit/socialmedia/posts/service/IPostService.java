package com.intuit.socialmedia.posts.service;

import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.dto.request.PostFilterRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;

import java.util.List;

public interface IPostService {
        List<PostResponse> getPosts(PostFilterRequest request);

        PostResponse upsertPost(PostCreateRequest request);

        boolean deletePost(String postId);
}