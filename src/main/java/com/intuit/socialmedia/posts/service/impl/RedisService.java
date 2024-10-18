package com.intuit.socialmedia.posts.service.impl;

import com.intuit.socialmedia.posts.entity.Post;
import com.intuit.socialmedia.posts.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Slf4j
public class RedisService implements IRedisService {


    @Value("${redis.maxList_size}")
    private int MAX_LIST_SIZE;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Method to get a value from Redis
    public Object getValue(String key) {
        if (Objects.isNull(key))
            return null;
        return redisTemplate.opsForValue().get(key);
    }

    // Method to set a value in Redis
    public void setValue(String key, Object value) {
        if (Objects.isNull(key))
            return;
        redisTemplate.opsForValue().set(key, value);
    }

    public void addToListAndTrim(String listKey, Object newValue) {
        if (Objects.isNull(listKey))
            return;
        // Perform LPUSH operation to add new element at the head of the list
        redisTemplate.opsForList().leftPush(listKey, newValue);

        // Perform TRIM operation to ensure list size does not exceed 100 elements
        redisTemplate.opsForList().trim(listKey, 0, MAX_LIST_SIZE - 1);
    }

    public List<Object> getList(String listKey) {
        List<Object> response = redisTemplate.opsForList().range(listKey, 0, -1);
        if (response == null)
            return new LinkedList<>();
        return response;
    }

    public Map<String, Object> multiGet(List<String> keys) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (Objects.isNull(keys) || keys.isEmpty())
            return map;
        List<Object> response = redisTemplate.opsForValue().multiGet(keys);
        if (response == null)
            return map;
        for (int i = 0; i < response.size(); ++i)
            map.put(keys.get(i), response.get(i));

        return map;
    }

    public void multiSet(Map<String, Object> keyVals) {
        if (keyVals.size() == 0)
            return;
        redisTemplate.opsForValue().multiSet(keyVals);
    }

    public void addList(String key, List<Object> value) { //rightPush cuz already recent_posts are sorted DESC
        if (!value.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, value);
        }
    }

    public void replaceList(String key, List<Object> value) {
        if (!value.isEmpty()) {
            // This removes all elements
            redisTemplate.delete(key);
            // Add the new values to the list
            redisTemplate.opsForList().rightPushAll(key, value);
        }
    }

}

