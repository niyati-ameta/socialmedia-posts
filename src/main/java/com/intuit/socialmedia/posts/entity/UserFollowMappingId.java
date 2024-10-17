package com.intuit.socialmedia.posts.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFollowMappingId implements Serializable { // todo why serializable
    private String follower;
    private String following;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFollowMappingId that)) return false;

        // Compare follower and following fields
        return follower.equals(that.follower) && following.equals(that.following);
    }
}