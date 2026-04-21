package com.marcelormdev.conduit_service.article;

import java.util.List;

public record ArticlesResponse(List<ArticleResponse.Params> articles, int articlesCount) {

    ArticlesResponse(List<ArticleResponse> responses) {
        this(responses.stream().map(ArticleResponse::article).toList(), responses.size());
    }

}
