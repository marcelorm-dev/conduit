package com.marcelormdev.conduit_service.article;

public record UpdateArticleRequest(Params article) {

    public record Params(String title, String description, String body, String[] tagList) {

    }
}
