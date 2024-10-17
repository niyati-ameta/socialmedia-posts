package com.intuit.socialmedia.posts.repository;

import com.intuit.socialmedia.posts.entity.UserFollowMapping;
import com.intuit.socialmedia.posts.entity.UserFollowMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFollowMappingDao extends JpaRepository<UserFollowMapping, UserFollowMappingId> {
    List<UserFollowMapping> findByIdFollowing(String following);
    List<UserFollowMapping> findByIdFollower(String follower);
}