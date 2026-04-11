package com.marcelormdev.conduit_service.apitest;

import org.springframework.test.web.servlet.client.RestTestClient;

public class ArticleRestCaller {

    private final RestCaller restCaller;

    public ArticleRestCaller(RestTestClient restClient) {
        this.restCaller = new RestCaller(restClient);
    }

    public RestTestClient.ResponseSpec callCreateArticleAPI(String token, String body) {
        return restCaller.post("/api/articles", token, body);
    }

    public RestTestClient.ResponseSpec callGetArticleAPI(String slug) {
        return callGetArticleAPI(slug, null);
    }

    public RestTestClient.ResponseSpec callGetArticleAPI(String slug, String token) {
        return restCaller.get("/api/articles/" + slug, token);
    }

    public RestTestClient.ResponseSpec callListArticlesAPI() {
        return callListArticlesAPI(null, null);
    }

    public RestTestClient.ResponseSpec callListArticlesAPI(String token) {
        return callListArticlesAPI(null, token);
    }

    public RestTestClient.ResponseSpec callListArticlesAPI(String queryParams, String token) {
        String uri = "/api/articles" + (queryParams != null ? "?" + queryParams : "");
        return restCaller.get(uri, token);
    }

    public RestTestClient.ResponseSpec callUpdateArticleAPI(String slug, String token, String body) {
        return restCaller.put("/api/articles/" + slug, token, body);
    }

    public RestTestClient.ResponseSpec callDeleteArticleAPI(String slug, String token) {
        return restCaller.delete("/api/articles/" + slug, token);
    }

}
