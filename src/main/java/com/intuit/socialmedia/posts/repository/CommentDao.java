package com.intuit.socialmedia.posts.repository;

import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentDao extends JpaRepository<Comment, String> {
    Slice<Comment> findByPostIdAndStatus(String postId, Status status, Pageable pageable);
}
