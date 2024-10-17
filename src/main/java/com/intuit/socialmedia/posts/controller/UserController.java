package com.intuit.socialmedia.posts.controller;

import com.intuit.socialmedia.posts.dto.request.RegisterNewUserRequest;
import com.intuit.socialmedia.posts.dto.request.UserLoginRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.dto.response.UserAuthResponse;
import com.intuit.socialmedia.posts.dto.response.UserResponse;
import com.intuit.socialmedia.posts.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/user")
@Slf4j
@Tag(name = "User Controller", description = "CRUD APIs related to User")
public class UserController {
    @Autowired
    UserService userServiceImpl;


    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "API to Create a new User")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully registered new user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> registerNewUser(@Valid @RequestBody RegisterNewUserRequest newUserRequest) {
        userServiceImpl.registerUser(newUserRequest);
        return new ResponseEntity<>("Successfully created user", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login User", description = "API to Login a User in the system")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully logged in user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthResponse> login(@Valid @RequestBody UserLoginRequest userLoginDto) throws Exception {
        UserAuthResponse response = userServiceImpl.login(userLoginDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{userEmail}")
    @Operation(summary = "Get User", description = "API to get User Details by email")
    public ResponseEntity<UserResponse> getUser(@Valid @NotBlank @PathVariable String userEmail) {
        UserResponse response = userServiceImpl.getUserByEmail(userEmail);
        return ResponseEntity.ok(response);
    }

}
