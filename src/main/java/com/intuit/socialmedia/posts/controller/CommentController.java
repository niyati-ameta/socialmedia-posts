package com.intuit.socialmedia.posts.controller;

import com.intuit.socialmedia.posts.dto.request.AddCommentRequest;
import com.intuit.socialmedia.posts.dto.request.CommentFilterRequest;
import com.intuit.socialmedia.posts.dto.response.CommentResponse;
import com.intuit.socialmedia.posts.service.ICommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/comment")
@Slf4j
@Tag(name = "Comment Controller", description = "CRUD APIs related to Comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<String> addComment(@PathVariable @NotBlank String postId, @RequestBody @Valid AddCommentRequest commentRequest) {
        log.info("Adding comment to postId {}: {}", postId, commentRequest);
        commentService.addComment(postId, commentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Comment added successfully to post " + postId);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(
            @PathVariable String postId, @RequestBody @Valid CommentFilterRequest commentFilterRequest) {
        log.info("GetComments request for postId: {}, page: {}, size: {}", postId, commentFilterRequest.getPageNumber(), commentFilterRequest.getPageNumber());
        List<CommentResponse> comments = commentService.getPaginatedCommentsByPostId(postId, commentFilterRequest.getPageSize(), commentFilterRequest.getPageNumber());
        return ResponseEntity.ok(comments);
    }

}