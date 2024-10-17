package com.intuit.socialmedia.posts.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserLoginRequest {
    @Email
    private String email;
    private String password;
}
