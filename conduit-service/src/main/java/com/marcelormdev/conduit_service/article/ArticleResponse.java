package com.marcelormdev.conduit_service.article;

import java.time.Instant;

import com.marcelormdev.conduit_service.profile.Profile;

public record ArticleResponse(Params article) {

    public ArticleResponse(Article article) {
        this(article, null);
    }

    public ArticleResponse(Article article, Profile currentUserProfile) {
        this(new Params(
                article.getSlug(),
                article.getTitle(),
                article.getDescription(),
                article.getBody(),
                article.getTags(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.isFavoritedBy(currentUserProfile),
                article.getFavoritesCount(),
                new AuthorParams(
                        article.getAuthor().getUsername(),
                        article.getAuthor().getBio(),
                        article.getAuthor().getImage(),
                        false)));
    }

    public record Params(String slug, String title, String description, String body, String[] tagList,
            Instant createdAt, Instant updatedAt, boolean favorited, int favoritesCount,
            AuthorParams author) {

    }

    public record AuthorParams(String username, String bio, String image, boolean following) {

    }
}
