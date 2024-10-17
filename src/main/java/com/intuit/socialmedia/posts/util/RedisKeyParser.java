package com.intuit.socialmedia.posts.util;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class RedisKeyParser implements KeyParser {
    public static String redisRecentPostsPerFollower = "social_post:recent_posts_follower:{userId}";
    public static String redisSnapPostsPerFollower = "social_post:snap_posts_follower:{userId}";
    public static String redisPost = "social_post:post:{postId}";

    public static String redisUserDetails = "social_post:user_details:{userId}";

    public static String redisCommentsPerPost = "social_post:comments_per_post:{postId}";


    @Override
    public String prepareKey(Map<String, String> valuesMap, String template) {
        String formattedString = template;
        for (Map.Entry<String, String> entry : valuesMap.entrySet())
            formattedString = StringUtils.replace(formattedString, "{" + entry.getKey() + "}", entry.getValue());

        return formattedString;
    }
}
