package com.intuit.socialmedia.posts.repository;

import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.dto.request.PostFilterRequest;
import com.intuit.socialmedia.posts.entity.Post;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface PostDao extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {
    @Query("SELECT p FROM Post p WHERE p.createdOn < :createdOn ORDER BY p.createdOn DESC")
    List<Post> findOlderPosts(OffsetDateTime createdOn, Pageable pageable);
    List<Post> findByIdIn(List<String> postIds);

    @Query("SELECT p FROM Post p WHERE p.createdOn < :createdOn AND p.status = 0 AND p.createdBy IN :userIds ORDER BY p.createdOn DESC")
    Page<Post> findOlderActivePostsByUsers(OffsetDateTime createdOn, List<String> userIds, Pageable pageable);
}
