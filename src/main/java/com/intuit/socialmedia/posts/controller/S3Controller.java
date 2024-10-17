package com.intuit.socialmedia.posts.controller;

import com.intuit.socialmedia.posts.auth.CustomUserDetails;
import com.intuit.socialmedia.posts.service.IS3PresignedUrlService;
import com.intuit.socialmedia.posts.service.impl.S3PresignedUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

@RestController
@RequestMapping("/v1/s3")
public class S3Controller {

    @Autowired
    private IS3PresignedUrlService s3PresignedUrlService;

    @GetMapping("/upload/{key}")
    public String generateUploadPresignedUrl(@PathVariable String key) {
        //add user id with key
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String keyWithFolder = userDetails.getId()+"/"+key;
        URL presignedUrl = s3PresignedUrlService.generatePresignedUrlForUpload(keyWithFolder);
        return presignedUrl.toString();
    }

    @GetMapping("/download/{key}")
    public String generateDownloadPresignedUrl(@PathVariable String key) {
        //add user id with key
        CustomUserDetails userDetails = (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String keyWithFolder = userDetails.getId()+"/"+key;
        URL presignedUrl = s3PresignedUrlService.generatePresignedUrlForDownload(keyWithFolder);
        return presignedUrl.toString();
    }
}
