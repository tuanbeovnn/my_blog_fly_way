package com.myblogbackend.blog.exception.commons;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements CommonErrorCode {
    ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Could not find the Id"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "Email or password is incorrect"),
    ALREADY_EXIST(HttpStatus.BAD_REQUEST, "Account already exist!"),
    USER_ACCOUNT_IS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "Account has not active yet"),
    USER_COULD_NOT_FOUND(HttpStatus.NOT_FOUND, "Account could not found"),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Could not found comment parent"),
    UNABLE_EDIT(HttpStatus.FORBIDDEN, "Unable to edit"),
    COULD_NOT_FOUND(HttpStatus.NOT_FOUND, "Could not found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Could not found comment"),
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "You cannot follow yourself"),
    PASSWORD_DOES_NOT_MATCH(HttpStatus.UNAUTHORIZED, "Password does not match"),
    PASSWORD_WRONG(HttpStatus.UNAUTHORIZED, "Password was wrong"),
    USER_ALREADY_LOGGED_IN(HttpStatus.FORBIDDEN, "User already logged in another device"),
    ACCOUNT_CREATED_LOCAL(HttpStatus.FORBIDDEN, "This account was created locally. Please use local login."),
    INVALID_AUTHORIZATION_CODE(HttpStatus.BAD_REQUEST, "The provided authorization code is invalid or expired."),
    GENERAL_AUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during authentication."),
    GOOGLE_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "Failed to authenticate with Google. Please try again later."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "error.could not send email");
    
    private final HttpStatus status;
    private final String message;

    private ErrorCode(final HttpStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return this.status;
    }

    public String message() {
        return this.message;
    }
}