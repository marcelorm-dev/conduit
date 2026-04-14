package com.marcelormdev.conduit_service.helpers;

import com.marcelormdev.conduit_service.article.ArticleResponse;
import com.marcelormdev.conduit_service.article.ArticleService;
import com.marcelormdev.conduit_service.article.CreateArticleRequest;

public class ArticleServiceTestHelper {

    private ArticleService articleService;

    private ArticleResponse articleResponse;

    private String token;

    ArticleServiceTestHelper(ArticleService articleService) {
        this.articleService = articleService;
    }

    ArticleServiceTestHelper createArticle(String token, String title, String description, String body,
            String[] tags) {
        this.token = token;
        this.articleResponse = articleService.create(token, new CreateArticleRequest(title, description, body, tags));
        return this;
    }

    public ArticleServiceTestHelper createArticle(String title, String description, String body, String[] tags) {
        return createArticle(this.token, title, description, body, tags);
    }

    public ArticleResponse getArticleResponse() {
        return articleResponse;
    }

    public String getToken() {
        return token;
    }

}
