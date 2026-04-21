package com.marcelormdev.conduit_service.common.exception;

public class ErrorMessages {

    private ErrorMessages() {

    }

    public static final String ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED = "Access denied. Token is invalid or expired";
    public static final String ACCESS_DENIED_TOKEN_NOT_INFORMED = "Access denied. Token must be informed";
    public static final String ACCESS_DENIED_EMAIL_NOT_FOUND = "Access denied. Email not found";
    public static final String EMAIL_IS_ALREADY_BEING_USED = "Email is already being used";
    public static final String EMAIL_NOT_INFORMED = "Email must be informed";
    public static final String EMAIL_NOT_FOUND = "Email not found";
    public static final String INVALID_EMAIL = "Email is invalid";
    public static final String INVALID_PASSWORD = "Password is invalid";
    public static final String PASSWORD_NOT_INFORMED = "Password must be informed";
    public static final String TOKEN_NOT_INFORMED = "Token must be informed";
    public static final String TOKEN_INVALID_OR_EXPIRED = "Token is invalid or expired";
    public static final String USERNAME_NOT_INFORMED = "Username must be informed";
    public static final String USERNAME_NOT_FOUND = "Username not found";
    public static final String TITLE_MUST_BE_INFORMED = "Title must be informed";
    public static final String DESCRIPTION_MUST_BE_INFORMED = "Description must be informed";
    public static final String BODY_MUST_BE_INFORMED = "Body must be informed";
    public static final String ARTICLE_NOT_FOUND = "not found";
    public static final String ARTICLE_SLUG_MUST_BE_INFORMED = "Article slug must be informed";
    public static final String FORBIDDEN_NOT_AUTHOR = "Forbidden. You are not the author of this article";

}
