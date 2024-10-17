package com.intuit.socialmedia.posts.dto.request;

import com.intuit.socialmedia.posts.constant.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostCreateRequest {
    private String id;

    @NotBlank
    private String description;

    private List<Media> mediaList;
    public record Media(@NotBlank String id, @NotNull MediaType type, @NotBlank String path) {}

}
