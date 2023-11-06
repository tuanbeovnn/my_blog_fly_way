package com.myblogbackend.blog.exception.commons;

import org.springframework.http.HttpStatus;

public enum ErrorCode implements CommonErrorCode {
    SUCCESS(HttpStatus.OK, "000", "Success"),
    FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "999", "System error"),
    ID_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "Could not find the Id"),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "API Not Found"),
    AUTHORIZATION_FIELD_MISSING(HttpStatus.FORBIDDEN, "40011", "Please log in"),
    SIGNATURE_NOT_CORRECT(HttpStatus.FORBIDDEN, "40001", "Signature not correct"),
    EXPIRED(HttpStatus.FORBIDDEN, "40003", "Expired"),
    UN_SUPPORT_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "40020", "Unsupport this file extension"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401", "Unauthorized"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "001", "validation.error"),
    ALREADY_EXIST(HttpStatus.BAD_REQUEST, "400", "Account already exist!"),
    USER_NAME_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "400", "Username or password not match!"),
    JWT_CLAIM_EMPTY(HttpStatus.UNAUTHORIZED, "401", "Claim empty"),
    ACCOUNT_NEEDS_TO_VERIFY(HttpStatus.BAD_REQUEST, "400", "Account needs to verify"),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "500", "error.could not send email");
    private final HttpStatus status;
    private final String code;
    private final String message;

    private ErrorCode(final HttpStatus status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String code() {
        return this.code;
    }

    public HttpStatus status() {
        return this.status;
    }

    public String message() {
        return this.message;
    }
}