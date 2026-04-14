package com.marcelormdev.conduit_service.common.exception;

public class ArticleNotFoundException extends ConduitApiException {

    public ArticleNotFoundException() {
        super(ErrorMessages.ARTICLE_NOT_FOUND);
    }

}
