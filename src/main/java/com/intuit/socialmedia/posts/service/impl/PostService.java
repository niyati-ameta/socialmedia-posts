package com.intuit.socialmedia.posts.service.impl;

import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.dto.request.PostFilterRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.service.IPostService;
import com.intuit.socialmedia.posts.service.impl.postmanagement.PostCreationService;
import com.intuit.socialmedia.posts.service.impl.postmanagement.PostDeletionService;
import com.intuit.socialmedia.posts.service.impl.postmanagement.PostRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PostService implements IPostService {
    private final PostCreationService postCreationService;
    private final PostRetrievalService postRetrievalService;
    private final PostDeletionService postDeletionService;

    @Autowired
    public PostService(PostCreationService postCreationService,
                       PostRetrievalService postRetrievalService,
                       PostDeletionService postDeletionService) {
        this.postCreationService = postCreationService;
        this.postRetrievalService = postRetrievalService;
        this.postDeletionService = postDeletionService;
    }


    @Override
    public PostResponse upsertPost(PostCreateRequest request) {
        return postCreationService.createPost(request);
    }

    @Override
    public List<PostResponse> getPosts(PostFilterRequest request) {
        return postRetrievalService.getPostsResponse(request);
    }

    @Override
    public boolean deletePost(String postId) {
        return postDeletionService.deletePost(postId);
    }
}