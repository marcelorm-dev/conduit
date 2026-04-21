package com.marcelormdev.conduit_service.article;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.common.exception.ArticleNotFoundException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.exception.ForbiddenException;
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
                .notNullOrBlank(request.article().description(),
                        ErrorMessages.DESCRIPTION_MUST_BE_INFORMED)
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
    public ArticleResponse favorite(String token, String slug) {
        Profile currentUserProfile = authService.authenticateProfile(token);

        Article article = findBySlug(slug);
        article.addFavorited(currentUserProfile);
        article = articleRepository.save(article);

        return new ArticleResponse(article, currentUserProfile);
    }

    @Transactional
    public ArticleResponse unfavorite(String token, String slug) {
        Profile currentUserProfile = authService.authenticateProfile(token);

        Article article = findBySlug(slug);
        article.removeFavorited(currentUserProfile);
        article = articleRepository.save(article);

        return new ArticleResponse(article, currentUserProfile);
    }

    @Transactional
    public void delete(String token, String slug) {
        Profile currentUserProfile = authService.authenticateProfile(token);

        Article article = findBySlug(slug);

        if (!article.isAuthoredBy(currentUserProfile.getUsername())) {
            throw new ForbiddenException(ErrorMessages.FORBIDDEN_NOT_AUTHOR);
        }

        articleRepository.delete(article);
    }

    private Article findBySlug(String slug) {
        new Validator()
                .notNullOrBlank(slug, ErrorMessages.ARTICLE_SLUG_MUST_BE_INFORMED)
                .throwViolations(FieldValidationException::new);

        return articleRepository.findBySlug(slug)
                .orElseThrow(ArticleNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public ArticleResponse getBySlug(String slug) {
        return new ArticleResponse(findBySlug(slug));
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> list(String token, String author, String tag, Boolean isFavorited) {
        Profile currentUserProfile = (token != null && !token.isBlank())
                ? authService.authenticateProfile(token)
                : null;

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
