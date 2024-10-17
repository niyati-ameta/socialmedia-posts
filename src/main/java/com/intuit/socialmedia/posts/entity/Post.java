package com.intuit.socialmedia.posts.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Post {

    @Id
    private String id;

    @NotNull
    @Size(max = 1000)
    private String description;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<PostCreateRequest.Media> mediaList;

    @Column(columnDefinition = "timestamptz")
    private OffsetDateTime createdOn;

    @Column(columnDefinition = "timestamptz")
    private OffsetDateTime updatedOn;

    private String createdBy; // userId

    @Enumerated(EnumType.ORDINAL)
    private Status status;
}
