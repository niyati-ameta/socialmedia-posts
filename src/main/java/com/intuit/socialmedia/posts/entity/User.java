package com.intuit.socialmedia.posts.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

//
@Entity
@Table(name = "user_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String name;
    private String profilePic;
    @Column(columnDefinition = "timestamptz")
    @CreationTimestamp
    private OffsetDateTime createdOn;
    @Column(columnDefinition = "timestamptz")
    @UpdateTimestamp
    private OffsetDateTime updatedOn;

}
