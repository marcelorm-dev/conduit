package com.marcelormdev.conduit_service.article;

import java.time.Instant;

public record ArticleResponse(Params article) {

        public ArticleResponse(Article article) {
                this(new Params(
                                article.getSlug(),
                                article.getTitle(),
                                article.getDescription(),
                                article.getBody(),
                                article.getTagList(),
                                article.getCreatedAt(),
                                article.getUpdatedAt(),
                                article.isFavorited(),
                                article.getFavoritesCount(),
                                new AuthorParams(
                                                article.getProfile().getUsername(),
                                                article.getProfile().getBio(),
                                                article.getProfile().getImage(),
                                                false)));
        }

        public record Params(String slug, String title, String description, String body, String[] tagList,
                        Instant createdAt, Instant updatedAt, boolean favorited, int favoritesCount,
                        AuthorParams author) {

        }

        public record AuthorParams(String username, String bio, String image, boolean following) {

        }

}
