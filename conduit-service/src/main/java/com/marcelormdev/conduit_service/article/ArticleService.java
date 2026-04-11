package com.marcelormdev.conduit_service.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public ArticleResponse create(String token, CreateArticleRequest request) {
        Profile profile = authService.authenticateProfile(token);

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
                profile);

        article = articleRepository.save(article);

        return new ArticleResponse(article);
    }

}
