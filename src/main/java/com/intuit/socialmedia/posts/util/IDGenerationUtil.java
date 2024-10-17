package com.intuit.socialmedia.posts.util;

import com.fasterxml.uuid.Generators;
import org.springframework.stereotype.Service;

@Service
public class IDGenerationUtil {
    public String generateUserId(String userName) {
        return userName+"_"+generateUUID();

    }
    public String generatePostId(String userName) {
        return userName+"_"+generateUUID();
    }

    private String generateUUID() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }
}
