package com.intuit.socialmedia.posts.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CommentFilterRequest {

    @Size(max = 100, min = 1, message = "page size value should be between 1 to 100 inclusive")
    private Integer pageSize;

    @Min(0)
    private Integer pageNumber;
}
