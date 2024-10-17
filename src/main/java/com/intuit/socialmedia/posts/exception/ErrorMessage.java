package com.intuit.socialmedia.posts.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
public class ErrorMessage {
    private int status;
    private Date timestamp;
    private String error;
    private String message;
    private String path;

    public ErrorMessage(HttpStatus httpStatus, String message, String description) {
        this.status = httpStatus.value();
        this.error = httpStatus.name();
        this.timestamp = new Date();
        this.message = message;
        this.path = description.replace("uri=", "");
    }

}