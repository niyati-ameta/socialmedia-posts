package com.intuit.socialmedia.posts.service.impl.postmanagement;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.entity.Post;
import com.intuit.socialmedia.posts.entity.UserFollowMapping;
import com.intuit.socialmedia.posts.exception.ResourceNotFoundException;
import com.intuit.socialmedia.posts.mapper.PostMapper;
import com.intuit.socialmedia.posts.model.RedisRecentPostObject;
import com.intuit.socialmedia.posts.repository.PostDao;
import com.intuit.socialmedia.posts.repository.UserFollowMappingDao;
import com.intuit.socialmedia.posts.service.impl.RedisService;
import com.intuit.socialmedia.posts.util.IDGenerationUtil;
import com.intuit.socialmedia.posts.util.RedisKeyParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PostCreationService {
    public static final ZoneId UTC_ZONEID = ZoneId.of("UTC");

    private final PostDao postDao;
    private final PostMapper postMapper;
    private final RedisService redisService;
    private final IDGenerationUtil idGenerationUtil;
    private final UserFollowMappingDao userFollowMappingDao;
    private final RedisKeyParser redisKeyParser;

    @Autowired
    public PostCreationService(PostDao postDao, PostMapper postMapper, RedisService redisService, IDGenerationUtil idGenerationUtil, UserFollowMappingDao userFollowMappingDao, RedisKeyParser redisKeyParser) {
        this.postDao = postDao;
        this.postMapper = postMapper;
        this.redisService = redisService;
        this.idGenerationUtil = idGenerationUtil;
        this.userFollowMappingDao = userFollowMappingDao;
        this.redisKeyParser = redisKeyParser;
    }

    public PostResponse createPost(PostCreateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Post postEntity;
        if(StringUtils.isNotBlank(request.getId())) {
            postEntity = postDao.findById(request.getId()).orElseThrow(()->new ResourceNotFoundException(request.getId()));
        } else {
            postEntity = Post.builder()
                    .createdOn(OffsetDateTime.now(UTC_ZONEID))
                    .id(idGenerationUtil.generatePostId(userDetails.getName())).build();
        }
        postEntity.setDescription(request.getDescription());
        postEntity.setMediaList(request.getMediaList());
        postEntity.setCreatedBy(userDetails.getId());
        postEntity.setStatus(Status.ACTIVE);
        postEntity.setUpdatedOn(OffsetDateTime.now(UTC_ZONEID));

        Post post = postDao.save(postEntity);
        saveToRedis(post);
        addToUserFollowersPosts(userDetails.getId(), post);
        return postMapper.postEntityToPostResponse(post);
    }

    private void saveToRedis(Post post) {
        redisKeyParser.prepareKey(Map.of("postId", post.getId()), RedisKeyParser.redisPost);
        redisService.setValue(redisKeyParser.prepareKey(Map.of("postId", post.getId()), RedisKeyParser.redisPost), post);
    }

    private void addToUserFollowersPosts(String userId, Post post) {
        //get all followers of this user
        List<UserFollowMapping> followMappings = userFollowMappingDao.findByIdFollowing(userId);

        for (UserFollowMapping followMapping : followMappings) {
            RedisRecentPostObject redisObject = RedisRecentPostObject.builder().postId(post.getId()).
                    createdOn(post.getCreatedOn()).createdByUserId(userId).build();
            redisService.addToListAndTrim(redisKeyParser.prepareKey(Map.of("userId", followMapping.getId().getFollower()), RedisKeyParser.redisRecentPostsPerFollower), redisObject);
        }
    }
}
