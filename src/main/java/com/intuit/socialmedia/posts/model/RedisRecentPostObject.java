package com.intuit.socialmedia.posts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisRecentPostObject {
    String postId;
    OffsetDateTime createdOn;
    String createdByUserId;
}
