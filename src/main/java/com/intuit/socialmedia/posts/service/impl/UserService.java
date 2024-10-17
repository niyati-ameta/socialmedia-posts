package com.intuit.socialmedia.posts.service.impl;

import com.intuit.socialmedia.posts.auth.JwtUtil;
import com.intuit.socialmedia.posts.dto.request.RegisterNewUserRequest;
import com.intuit.socialmedia.posts.dto.request.UserLoginRequest;
import com.intuit.socialmedia.posts.dto.response.UserAuthResponse;
import com.intuit.socialmedia.posts.dto.response.UserResponse;
import com.intuit.socialmedia.posts.entity.User;
import com.intuit.socialmedia.posts.exception.ResourceNotFoundException;
import com.intuit.socialmedia.posts.mapper.UserMapper;
import com.intuit.socialmedia.posts.repository.UserDao;
import com.intuit.socialmedia.posts.service.IUserService;
import com.intuit.socialmedia.posts.util.IDGenerationUtil;
import com.intuit.socialmedia.posts.util.RedisKeyParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements IUserService {
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDao userDao;
    private final UserMapper userMapper;
    private final IDGenerationUtil idGenerationUtil;
    private final RedisKeyParser keyParser;

    @Autowired
    public UserService(RedisService redisService,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       UserDao userDao,
                       UserMapper userMapper,
                       IDGenerationUtil idGenerationUtil,
                       RedisKeyParser redisKeyParser) {
        this.redisService = redisService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDao = userDao;
        this.userMapper = userMapper;
        this.idGenerationUtil = idGenerationUtil;
        this.keyParser = redisKeyParser;
    }

    public void registerUser(RegisterNewUserRequest newUserRequest) {
        userDao.findByEmail(newUserRequest.getEmail())
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("User already registered!");
                });

        User userEntity = userMapper.requestToUserEntity(newUserRequest);
        userEntity.setPassword(passwordEncoder.encode(newUserRequest.getPassword()));
        userEntity.setId(idGenerationUtil.generateUserId(newUserRequest.getName()));
        User savedUserEntity = userDao.save(userEntity);
        log.info("Caching user id to details in redis ..");
        redisService.setValue(keyParser.prepareKey(Map.of("userId", savedUserEntity.getId()), RedisKeyParser.redisUserDetails), savedUserEntity.getName());
    }

    public UserAuthResponse login(UserLoginRequest userLoginDto) throws Exception {
        try {
            Optional<User> userOptional = userDao.findByEmail(userLoginDto.getEmail());
            User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found!"));
            boolean authenticated =  passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword());

            if (!authenticated) {
                throw new BadCredentialsException("Authentication Issue");
            }
        } catch (BadCredentialsException bce) {
            throw new BadCredentialsException("User email or password incorrect!");
        }

        User userEntity = userDao.findByEmail(userLoginDto.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        UserAuthResponse userAuthResponseObject = userMapper.entityToUserAuthResponse(userEntity);
        userAuthResponseObject.setToken(jwtUtil.generateToken(userLoginDto));
        return userAuthResponseObject;
    }

    public UserResponse getUser(String userId) {
        Optional<User> userEntity = userDao.findById(userId);
        if (userEntity.isPresent()) {
            return userMapper.entityToUserResponse(userEntity.get());
        }
        log.error("User not found with id: {}", userId);
        throw new ResourceNotFoundException("User not found with id: " + userId);
    }

    public UserResponse getUserByEmail(String userEmail) {
        Optional<User> userEntity = userDao.findByEmail(userEmail);
        if (userEntity.isPresent()) {
            return userMapper.entityToUserResponse(userEntity.get());
        }
        log.error("User not found with email: {}", userEmail);
        throw new ResourceNotFoundException("User not found with email: " + userEmail);
    }
}


