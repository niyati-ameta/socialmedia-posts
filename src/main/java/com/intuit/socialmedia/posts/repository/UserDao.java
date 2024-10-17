package com.intuit.socialmedia.posts.repository;

import com.intuit.socialmedia.posts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, String> {
    // Method to find users by a list of IDs
    List<User> findByIdIn(List<String> ids);
    Optional<User> findByEmail(String email);

}
