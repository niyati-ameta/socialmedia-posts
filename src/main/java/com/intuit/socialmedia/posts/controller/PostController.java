package com.intuit.socialmedia.posts.controller;

import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.dto.request.PostFilterRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.service.IPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("v1/post")
@Tag(name = "Post Controller", description = "CRUD APIs related to Post")
public class PostController {

    @Autowired
    IPostService postService;

    @PostMapping("/upsert")
    @Operation(summary = "Create or update post", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostCreateRequest request, @RequestHeader("Authorization") String authHeader) {
        PostResponse response = postService.upsertPost(request);
        log.info("Created post successfully!");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/list")
    public ResponseEntity<List<PostResponse>> list(@RequestBody @Valid PostFilterRequest filterRequest) {
        log.info("PostFilterRequest request : {}", filterRequest);
        List<PostResponse> responseList = postService.getPosts(filterRequest);
        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable String postId) {
        log.info("deleting postId {}", postId);
        postService.deletePost(postId);
        return ResponseEntity.status(HttpStatus.OK).body("Post successfully deleted " + postId);
    }
}
