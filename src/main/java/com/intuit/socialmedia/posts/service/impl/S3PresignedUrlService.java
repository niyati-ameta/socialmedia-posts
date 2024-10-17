package com.intuit.socialmedia.posts.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class S3PresignedUrlService {

    private final S3Presigner s3Presigner;
    @Value("${s3.socialPost.bucket}")
    private String bucketName;
    @Value("${s3.uploadPresignedUrl.expiry}")
    private Long uploadPresignedUrl;

    @Value("${s3.downloadPresignedUrl.expiry}")
    private Long downloadPresignedUrl;
    @Autowired
    public S3PresignedUrlService(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public URL generatePresignedUrlForUpload(String objectKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(uploadPresignedUrl)) // URL validity
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    public URL generatePresignedUrlForDownload(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(downloadPresignedUrl)) // URL validity
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }
}
