package com.intuit.socialmedia.posts.service;

import java.util.List;
import java.util.Map;

public interface IRedisService {
    Object getValue(String key);

    void setValue(String key, Object value);

    void addToListAndTrim(String listKey, Object newValue);

    List<Object> getList(String listKey);

    Map<String, Object> multiGet(List<String> keys);

    void multiSet(Map<String, Object> keyVals);

    void addList(String key, List<Object> value);

    void replaceList(String key, List<Object> value);
}
