package com.intuit.socialmedia.posts.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_follow_mapping")
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFollowMapping {

   @EmbeddedId
   private UserFollowMappingId id; // Composite primary key

   @Column(columnDefinition = "timestamptz")
   @CreationTimestamp
   private OffsetDateTime createdOn;

   public UserFollowMapping(UserFollowMappingId userFollowMappingId) {
      this.setId(userFollowMappingId);
   }
}
