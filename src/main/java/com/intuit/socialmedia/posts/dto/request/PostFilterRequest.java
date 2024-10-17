package com.intuit.socialmedia.posts.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PostFilterRequest {
    @NotNull
    private Long epoch;

    @Min(value = 1, message = "page size value should be at least 1")
    @Max(value = 100, message = "page size value should be at most 100")
    private int pageSize;
}
