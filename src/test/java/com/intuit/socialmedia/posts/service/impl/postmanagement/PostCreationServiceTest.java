package com.intuit.socialmedia.posts.service.impl.postmanagement;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.constant.MediaType;
import com.intuit.socialmedia.posts.constant.Status;
import com.intuit.socialmedia.posts.dto.request.PostCreateRequest;
import com.intuit.socialmedia.posts.dto.response.PostResponse;
import com.intuit.socialmedia.posts.entity.Post;
import com.intuit.socialmedia.posts.exception.ResourceNotFoundException;
import com.intuit.socialmedia.posts.mapper.PostMapper;
import com.intuit.socialmedia.posts.repository.PostDao;
import com.intuit.socialmedia.posts.repository.UserFollowMappingDao;
import com.intuit.socialmedia.posts.service.impl.RedisService;
import com.intuit.socialmedia.posts.util.IDGenerationUtil;
import com.intuit.socialmedia.posts.util.RedisKeyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.intuit.socialmedia.posts.service.impl.postmanagement.PostCreationService.UTC_ZONEID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostCreationServiceTest {

    @Mock
    private PostDao postDao;

    @Mock
    private PostMapper postMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private IDGenerationUtil idGenerationUtil;

    @Mock
    private UserFollowMappingDao userFollowMappingDao;

    @Mock
    private RedisKeyParser redisKeyParser;

    @InjectMocks
    private PostCreationService postCreationService;


    CustomUserDetails userDetails;

    private static final String USER_ID = "test_user_id";
    private static final String USER_NAME = "test_user_name";
    private static final String MEDIA_ID = "media_id_1";
    private static final String POST_DESCRIPTION = "post_description";
    private static final String NEW_POST_ID = "new_post_id";
    private static final String EXISTING_POST_ID = "existing_post_id";
    private static final String UPDATED_POST_DESCRIPTION = "updated_post_description";
    private static final String NON_EXISTENT_POST_ID = "non_existent_post";
    private static final List<PostCreateRequest.Media> MEDIA_LIST = List.of(
            new PostCreateRequest.Media(MEDIA_ID, MediaType.IMAGE, "path/to/image1")
    );


    @BeforeEach
    public void setUp() {
        userDetails = new CustomUserDetails(USER_ID, USER_NAME, "valid_email@gmail.com", "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null));
    }
    private PostCreateRequest createPostRequest(String id, String description) {
        return PostCreateRequest.builder()
                .id(id)
                .description(description)
                .mediaList(MEDIA_LIST)
                .build();
    }

    private PostResponse createPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .description(post.getDescription())
                .mediaList(post.getMediaList())
                .createdOn(post.getCreatedOn())
                .updatedOn(post.getUpdatedOn())
                .createdById(post.getCreatedBy())
                .createdByName(USER_NAME)
                .comments(List.of())
                .build();
    }

    private Post createPostEntity(String id, String description) {
        return Post.builder()
                .id(id)
                .createdBy(USER_ID)
                .description(description)
                .createdOn(OffsetDateTime.now(UTC_ZONEID))
                .updatedOn(OffsetDateTime.now(UTC_ZONEID))
                .status(Status.ACTIVE)
                .mediaList(MEDIA_LIST)
                .build();
    }

    @Test
    public void testCreatePost_NewPost() {
        PostCreateRequest request = createPostRequest(null, POST_DESCRIPTION);
        Post postEntity = createPostEntity(NEW_POST_ID, POST_DESCRIPTION);
        PostResponse expectedResponse = createPostResponse(postEntity);

        when(idGenerationUtil.generatePostId(anyString())).thenReturn(NEW_POST_ID);
        when(postMapper.postEntityToPostResponse(any(Post.class))).thenReturn(expectedResponse);
        when(postDao.save(any(Post.class))).thenReturn(postEntity);
        when(redisKeyParser.prepareKey(anyMap(), anyString())).thenReturn("expectedRedisKey");

        PostResponse response = postCreationService.createPost(request);

        assertEquals(expectedResponse, response);
        verify(postDao).save(argThat(post -> {
            assertEquals(POST_DESCRIPTION, post.getDescription());
            assertEquals(NEW_POST_ID, post.getId());
            assertEquals(USER_ID, post.getCreatedBy());
            return true;
        }));
        verify(redisService).setValue(anyString(), eq(postEntity));
    }

    @Test
    public void testCreatePost_UpdateExistingPost() {
        PostCreateRequest request = createPostRequest(EXISTING_POST_ID, UPDATED_POST_DESCRIPTION);
        Post existingPost = createPostEntity(EXISTING_POST_ID, "old_post_description");
        when(postDao.findById(EXISTING_POST_ID)).thenReturn(Optional.of(existingPost));

        Post updatedPost = createPostEntity(EXISTING_POST_ID, UPDATED_POST_DESCRIPTION);
        PostResponse expectedResponse = createPostResponse(updatedPost);
        when(postMapper.postEntityToPostResponse(any(Post.class))).thenReturn(expectedResponse);
        when(postDao.save(any(Post.class))).thenReturn(updatedPost);
        when(redisKeyParser.prepareKey(anyMap(), anyString())).thenReturn("expectedRedisKey");
        when(userFollowMappingDao.findByIdFollowing(anyString())).thenReturn(Collections.emptyList());

        PostResponse response = postCreationService.createPost(request);

        assertNotNull(response);
        assertEquals(EXISTING_POST_ID, response.getId());
        assertEquals(UPDATED_POST_DESCRIPTION, response.getDescription());
        assertEquals(MEDIA_LIST, response.getMediaList());

        verify(postDao).findById(EXISTING_POST_ID);
        verify(postMapper).postEntityToPostResponse(updatedPost);
        verify(redisService).setValue(anyString(), eq(updatedPost));
        verify(userFollowMappingDao).findByIdFollowing(USER_ID);
    }


    @Test
    public void testCreatePost_ResourceNotFound() {
        when(postDao.findById(NON_EXISTENT_POST_ID)).thenReturn(Optional.empty());
        PostCreateRequest request = createPostRequest(NON_EXISTENT_POST_ID, POST_DESCRIPTION);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> postCreationService.createPost(request));

        assertEquals(NON_EXISTENT_POST_ID, exception.getMessage());
    }


}
