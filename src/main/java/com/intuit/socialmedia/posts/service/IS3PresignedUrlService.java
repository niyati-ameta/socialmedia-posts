package com.intuit.socialmedia.posts.service;

import java.net.URL;

public interface IS3PresignedUrlService {
    URL generatePresignedUrlForUpload(String objectKey);

    URL generatePresignedUrlForDownload(String objectKey);
}
