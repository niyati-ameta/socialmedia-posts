package com.intuit.socialmedia.posts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SocialmediaPostsApplication {

	//http://localhost:8080/social-posts/api/swagger-ui/index.html#/
	public static void main(String[] args) {
		try {
			SpringApplication.run(SocialmediaPostsApplication.class, args);
		} catch (Throwable t) {
			log.error("Error in main flow ", t);
		}
	}
}
