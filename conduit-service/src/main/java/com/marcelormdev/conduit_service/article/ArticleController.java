package com.marcelormdev.conduit_service.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.marcelormdev.conduit_service.common.http.AuthorizationHeader;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping("/api/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@RequestHeader HttpHeaders headers, @RequestBody CreateArticleRequest request) {
        String token = new AuthorizationHeader(headers).getToken();
        return articleService.create(token, request);
    }

    @GetMapping("/api/articles/{slug}")
    public ArticleResponse getArticle(@PathVariable String slug) {
        return articleService.getBySlug(slug);
    }

    @PostMapping("/api/articles/{slug}/favorite")
    public ArticleResponse favorite(@RequestHeader HttpHeaders headers, @PathVariable String slug) {
        String token = new AuthorizationHeader(headers).getToken();
        return articleService.favorite(token, slug);
    }

    // Update Article - PUT /api/articles/:slug

    // Delete Article - DELETE /api/articles/:slug

    // Feed Articles - GET /api/articles/feed

    // List Articles - GET /api/articles

}
