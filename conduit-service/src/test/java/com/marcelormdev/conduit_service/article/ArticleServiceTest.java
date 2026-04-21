package com.marcelormdev.conduit_service.article;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.ArticleNotFoundException;
import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.exception.ForbiddenException;
import com.marcelormdev.conduit_service.helpers.UserServiceTestHelper;
import com.marcelormdev.conduit_service.helpers.TestHelper;

import com.marcelormdev.conduit_service.user.UserRepository;

@SpringBootTest
class ArticleServiceTest {

    @Autowired
    private TestHelper helper;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void beforeEachTest() {
        userRepository.deleteAll();
    }

    @Test
    void create_returnsArticle_whenInputIsValid() {
        ArticleResponse response = helper
                .register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body content", new String[] { "tag1", "tag2" })
                .getArticleResponse();

        assertEquals("Test Article", response.article().title());
        assertEquals("Test description", response.article().description());
        assertEquals("Test body content", response.article().body());
        assertArrayEquals(new String[] { "tag1", "tag2" }, response.article().tagList());
        assertTrue(response.article().slug().contains("test-article"));
        assertNotNull(response.article().createdAt());
        assertFalse(response.article().favorited());
        assertEquals(0, response.article().favoritesCount());
        assertEquals("author", response.article().author().username());
    }

    @Test
    void create_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> articleService.create(nullOrBlank,
                            new CreateArticleRequest("Title", "Desc", "Body", new String[] {})));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void create_throwsException_whenTokenIsInvalid() {
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> articleService.create("invalid-token",
                        new CreateArticleRequest("Title", "Desc", "Body", new String[] {})));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void create_throwsException_whenTitleIsNullOrBlank() {
        String token = helper.register("author", "author@test.com", "123456").getToken();

        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> articleService.create(token, new CreateArticleRequest(
                            nullOrBlank, "desc", "body", new String[] {})));
            assertEquals(ErrorMessages.TITLE_MUST_BE_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void create_throwsException_whenDescriptionIsNullOrBlank() {
        String token = helper.register("author", "author@test.com", "123456").getToken();

        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> articleService.create(token, new CreateArticleRequest(
                            "Article", nullOrBlank, "body", new String[] {})));
            assertEquals(ErrorMessages.DESCRIPTION_MUST_BE_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void create_throwsException_whenBodyIsNullOrBlank() {
        String token = helper.register("author", "author@test.com", "123456").getToken();

        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> articleService.create(token, new CreateArticleRequest(
                            "Article", "desc", nullOrBlank, new String[] {})));
            assertEquals(ErrorMessages.BODY_MUST_BE_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void create_withEmptyTagList_returnsArticleWithNoTags() {
        ArticleResponse response = helper
                .register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body content", new String[] {})
                .getArticleResponse();

        assertEquals(0, response.article().tagList().length);
    }

    @Test
    void getBySlug_returnsArticle_whenSlugExists() {
        String slug = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body content", new String[] {}).getSlug();

        ArticleResponse response = articleService.getBySlug(slug);

        assertEquals("Test Article", response.article().title());
        assertEquals(slug, response.article().slug());
        assertEquals("Test description", response.article().description());
        assertEquals("Test body content", response.article().body());
        assertEquals(0, response.article().tagList().length);
        assertFalse(response.article().favorited());
        assertEquals(0, response.article().favoritesCount());
        assertEquals("author", response.article().author().username());
    }

    @Test
    void getBySlug_throwsException_whenSlugNotFound() {
        ArticleNotFoundException exception = assertThrowsExactly(ArticleNotFoundException.class,
                () -> articleService.getBySlug("non-existent-slug"));
        assertEquals(ErrorMessages.ARTICLE_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void list_returnsAllArticles_withoutFilters() {
        String token = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body content",
                        new String[] { "tag1", "tag2" })
                .getToken();

        List<ArticleResponse> articles = articleService.list(token, null, null, null);

        assertFalse(articles.isEmpty());
    }

    @Test
    void list_filtersByAuthor() {
        String token1 = helper.register("author", "author@test.com", "123456")
                .createArticle("Title 1", "Desc 1", "Content 1", new String[] {})
                .getToken();

        helper.register("author2", "author2@test.com", "123456")
                .createArticle("Title 2", "Desc 2", "Content 2", new String[] {});

        List<ArticleResponse> articles = articleService.list(token1, "author", null, null);

        assertEquals(1, articles.size());
        assertEquals("author", articles.get(0).article().author().username());
    }

    @Test
    void list_filtersByTag() {
        String token = helper.register("author", "author@test.com", "123456")
                .createArticle("A", "d", "b", new String[] { "java" })
                .createArticle("B", "d", "b", new String[] { "spring" })
                .getToken();

        List<ArticleResponse> articles = articleService.list(token, null, "java", null);

        assertEquals(1, articles.size());
    }

    @Test
    void list_filtersByFavoritedBy() {
        helper.register("author", "author@test.com", "123456")
                .createArticle("Favorited", "desc", "body", new String[] {})
                .createArticle("Not Favorited", "desc", "body", new String[] {});

        String visitorToken = helper.register("visitor", "visitor@test.com", "123456").getToken();
        articleService.favorite(visitorToken, "favorited");

        List<ArticleResponse> articles = articleService.list(visitorToken, null, null, true);

        assertEquals(1, articles.size());
        assertEquals("Favorited", articles.get(0).article().title());
        assertTrue(articles.get(0).article().favorited());
        assertEquals(1, articles.get(0).article().favoritesCount());
    }

    @Test
    void list_returnsAtMost20Articles_whenMoreThan20Exist() {
        UserServiceTestHelper userServHelper = helper.register("author", "author@test.com", "123456");
        String token = userServHelper.getToken();

        for (int i = 1; i <= 21; i++) {
            userServHelper.createArticle("Article " + i, "desc", "body", new String[] {});
        }

        List<ArticleResponse> articles = articleService.list(token, null, null, null);

        assertEquals(20, articles.size());
    }

    @Test
    void favorite_returnsFavoritedTrue_withFavoritesCountOne_whenArticleIsFavorited() {
        String slug = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "desc", "body", new String[] {})
                .getSlug();

        String readerToken = helper.register("reader", "reader@test.com", "123456").getToken();
        articleService.favorite(readerToken, slug);

        List<ArticleResponse> articles = articleService.list(readerToken, null, null, null);

        assertEquals(1, articles.size());
        assertTrue(articles.get(0).article().favorited());
        assertEquals(1, articles.get(0).article().favoritesCount());
    }

    @Test
    void favorite_throwsException_whenSlugIsNullOrBlank() {
        String token = helper.register("user", "user@test.com", "123456").getToken();

        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> articleService.favorite(token, nullOrBlank));
            assertEquals(ErrorMessages.ARTICLE_SLUG_MUST_BE_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void unfavorite_returnsFavoritedFalse_whenPreviouslyFavorited() {
        String slug = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body content", new String[] {})
                .getSlug();

        String readerToken = helper.register("reader", "reader@test.com", "123456").getToken();
        articleService.favorite(readerToken, slug);

        ArticleResponse response = articleService.unfavorite(readerToken, slug);

        assertFalse(response.article().favorited());
        assertEquals(0, response.article().favoritesCount());
    }

    @Test
    void unfavorite_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> articleService.unfavorite(nullOrBlank, "some-slug"));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void unfavorite_throwsException_whenTokenIsInvalid() {
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> articleService.unfavorite("invalid-token", "some-slug"));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void unfavorite_throwsException_whenSlugIsNullOrBlank() {
        String token = helper.register("user", "user@test.com", "123456").getToken();

        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> articleService.unfavorite(token, nullOrBlank));
            assertEquals(ErrorMessages.ARTICLE_SLUG_MUST_BE_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void unfavorite_throwsException_whenSlugNotFound() {
        String token = helper.register("user", "user@test.com", "123456").getToken();

        ArticleNotFoundException exception = assertThrowsExactly(ArticleNotFoundException.class,
                () -> articleService.unfavorite(token, "non-existent-slug"));
        assertEquals(ErrorMessages.ARTICLE_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void list_doesNotIncludeBodyInResponse() {
        String token = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body content", new String[] {})
                .getToken();

        List<ArticleResponse> responses = articleService.list(token, null, null, null);

        assertEquals(1, responses.size());
        assertNull(responses.get(0).article().body());
    }

    @Test
    void update_returnsUpdatedArticle_whenBodyIsChanged() {
        var articleHelper = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body", new String[] {});
        String token = articleHelper.getToken();
        String slug = articleHelper.getSlug();
        ArticleResponse created = articleService.getBySlug(slug);

        ArticleResponse response = articleService.update(token, slug,
                new UpdateArticleRequest(new UpdateArticleRequest.Params(null, null, "Updated body", null)));

        assertEquals("Updated body", response.article().body());
        assertEquals("Test Article", response.article().title());
        assertNotEquals(created.article().updatedAt(), response.article().updatedAt());
        assertEquals(created.article().createdAt(), response.article().createdAt());
    }

    @Test
    void update_updatesSlug_whenTitleIsChanged() {
        var articleHelper = helper.register("author", "author@test.com", "123456")
                .createArticle("Old Title", "Test description", "Test body", new String[] {});
        String token = articleHelper.getToken();
        String slug = articleHelper.getSlug();

        ArticleResponse response = articleService.update(token, slug,
                new UpdateArticleRequest(new UpdateArticleRequest.Params("New Title", null, null, null)));

        assertEquals("New Title", response.article().title());
        assertEquals("new-title", response.article().slug());
    }

    @Test
    void update_preservesTags_whenTagListIsAbsentFromRequest() {
        var articleHelper = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body", new String[] { "tag1", "tag2" });
        String token = articleHelper.getToken();
        String slug = articleHelper.getSlug();

        ArticleResponse response = articleService.update(token, slug,
                new UpdateArticleRequest(new UpdateArticleRequest.Params(null, null, "New body", null)));
        
        assertEquals(2, response.article().tagList().length);
        assertEquals("tag1", response.article().tagList()[0]);
        assertEquals("tag2" , response.article().tagList()[1]);
    }

    @Test
    void update_clearsTags_whenTagListIsEmptyArray() {
        var articleHelper = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "Test description", "Test body", new String[] { "tag1", "tag2" });
        String token = articleHelper.getToken();
        String slug = articleHelper.getSlug();

        ArticleResponse response = articleService.update(token, slug,
                new UpdateArticleRequest(new UpdateArticleRequest.Params(null, null, null, new String[] {})));

        assertEquals(0, response.article().tagList().length);
    }

    @Test
    void update_throwsException_whenTokenIsInvalid() {
        var articleHelper = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "desc", "body", new String[] {});
        String slug = articleHelper.getSlug();

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> articleService.update("invalid-token", slug,
                        new UpdateArticleRequest(new UpdateArticleRequest.Params(null, null, "Updated body", null))));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
        }    

    @Test
    void delete_removesArticle_whenAuthorDeletesOwnArticle() {
        var authorHelper = helper.register("author", "author@test.com", "123456");
        String slug = authorHelper.createArticle("Test Article", "desc", "body", new String[] {}).getSlug();
        String token = authorHelper.getToken();

        articleService.delete(token, slug);

        assertThrowsExactly(ArticleNotFoundException.class, () -> articleService.getBySlug(slug));
    }

    @Test
    void delete_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> articleService.delete(nullOrBlank, "some-slug"));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void delete_throwsException_whenTokenIsInvalid() {
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> articleService.delete("invalid-token", "some-slug"));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void delete_throwsException_whenSlugIsNullOrBlank() {
        String token = helper.register("author", "author@test.com", "123456").getToken();

        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> articleService.delete(token, nullOrBlank));
            assertEquals(ErrorMessages.ARTICLE_SLUG_MUST_BE_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void delete_throwsException_whenSlugNotFound() {
        String token = helper.register("author", "author@test.com", "123456").getToken();

        assertThrowsExactly(ArticleNotFoundException.class,
                () -> articleService.delete(token, "non-existent-slug"));
    }

    @Test
    void delete_throwsException_whenUserIsNotAuthor() {
        String slug = helper.register("author", "author@test.com", "123456")
                .createArticle("Test Article", "desc", "body", new String[] {})
                .getSlug();

        String otherToken = helper.register("other", "other@test.com", "123456").getToken();

        ForbiddenException exception = assertThrowsExactly(ForbiddenException.class,
                () -> articleService.delete(otherToken, slug));
        assertEquals(ErrorMessages.FORBIDDEN_NOT_AUTHOR, exception.getMessagesAsString());
    }

}
