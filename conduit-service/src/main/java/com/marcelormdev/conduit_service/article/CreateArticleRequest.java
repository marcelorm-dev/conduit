package com.marcelormdev.conduit_service.article;

public record CreateArticleRequest(Params article) {

    public CreateArticleRequest(String title, String description, String body, String[] tagList) {
        this(new Params(title, description, body, tagList));
    }

    public record Params(String title, String description, String body, String[] tagList) {

    }
}
