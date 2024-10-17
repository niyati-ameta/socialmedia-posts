package com.intuit.socialmedia.posts.util;

import java.util.Map;

public interface KeyParser {
    String prepareKey(Map<String, String> params, String type);

}
