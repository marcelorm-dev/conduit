package com.marcelormdev.conduit_service.article;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.validation.Validator;
import com.marcelormdev.conduit_service.profile.Profile;

@Service
public class ArticleService {

    @Autowired
    private AuthService authService;

    @Autowired
    private ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse create(String token, CreateArticleRequest request) {
        Profile currentUserProfile = authService.authenticateProfile(token);

        new Validator()
                .notNullOrBlank(request.article().title(), ErrorMessages.TITLE_MUST_BE_INFORMED)
                .notNullOrBlank(request.article().description(), ErrorMessages.DESCRIPTION_MUST_BE_INFORMED)
                .notNullOrBlank(request.article().body(), ErrorMessages.BODY_MUST_BE_INFORMED)
                .throwViolations(FieldValidationException::new);

        Article article = new Article(
                request.article().title(),
                request.article().description(),
                request.article().body(),
                request.article().tagList(),
                currentUserProfile);

        article = articleRepository.save(article);

        return new ArticleResponse(article, currentUserProfile);
    }

    @Transactional
    public ArticleResponse favorite(String token, String articleSlug) {
        Profile currentUserProfile = authService.authenticateProfile(token);

        new Validator()
                .notNullOrBlank(articleSlug, ErrorMessages.ARTICLE_SLUG_MUST_BE_INFORMED)
                .throwViolations(FieldValidationException::new);

        Article article = articleRepository.findBySlug(articleSlug)
                .orElseThrow(() -> new FieldValidationException(ErrorMessages.ARTICLE_NOT_FOUND));

        article.addFavorited(currentUserProfile);

        return new ArticleResponse(article, currentUserProfile);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> list(String token, String author, String tag, Boolean isFavorited) {
        Profile currentUserProfile = authService.authenticateProfile(token);

        return articleRepository
                .findAll()
                .stream()
                .filter(article -> author == null || article.isAuthoredBy(author))
                .filter(article -> tag == null || article.hasTag(tag))
                .filter(article -> isFavorited == null || article.isFavoritedBy(currentUserProfile))
                .map(article -> new ArticleResponse(article, currentUserProfile))
                .limit(20)
                .toList();
    }

}
