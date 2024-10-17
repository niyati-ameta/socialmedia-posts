package com.intuit.socialmedia.posts.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intuit.socialmedia.posts.constant.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comment")
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Size(max = 200, min = 2, message = "Comment must be min 2 and max 200 character long")
    @NotBlank(message = "Comment should not be blank")
    private String body; // no media

    //userId
    private String createdBy;

    @Column(columnDefinition = "timestamptz")
    @CreationTimestamp
    private OffsetDateTime createdOn;

    @Column(columnDefinition = "timestamptz")
    @UpdateTimestamp
    private OffsetDateTime updatedOn;

    private String postId;

    private Status status;

}
