package com.intuit.socialmedia.posts.dto.request;

import com.intuit.socialmedia.posts.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterNewUserRequest {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @ValidPassword
    private String password;

    @NotBlank(message = "Name is mandatory")
    private String name;

    private String profilePic; // Optional, can be null
}
