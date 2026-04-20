package com.marcelormdev.conduit_service.article;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.marcelormdev.conduit_service.helpers.UserServiceTestHelper;
import com.marcelormdev.conduit_service.helpers.TestHelper;

import com.marcelormdev.conduit_service.profile.ProfileRepository;
import com.marcelormdev.conduit_service.user.UserRepository;

@SpringBootTest
class ArticleServiceTest {

    @Autowired
    private TestHelper helper;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void beforeEachTest() {
        profileRepository.deleteAll();
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

    // Not yet implemented: response should not include body in list
    //
    // @Test
    // void list_doesNotIncludeBodyInResponse() { ... }

    // --- Update Article ---

    // Not yet implemented: articleService.update(slug, token, request)
    //
    // @Test
    // void update_returnsUpdatedArticle_whenBodyIsChanged() {
    // String token = registerAndGetToken("author", "author@test.com");
    // String slug = createArticle(token).article().slug();
    //
    // ArticleResponse response = articleService.update(slug, token, new
    // UpdateArticleRequest("Updated body"));
    //
    // assertEquals("Updated body", response.article().body());
    // assertEquals("Test Article", response.article().title());
    // }

    // Not yet implemented: articleService.update(slug, token, request) — tags
    // absent means preserved
    //
    // @Test
    // void update_preservesTags_whenTagListIsAbsentFromRequest() {
    // String token = registerAndGetToken("author", "author@test.com");
    // String slug = createArticle(token).article().slug();
    //
    // ArticleResponse response = articleService.update(slug, token,
    // bodyOnlyRequest("New body"));
    //
    // assertEquals(2, response.article().tagList().length);
    // }

    // Not yet implemented: articleService.update(slug, token, request) — empty
    // array clears tags
    //
    // @Test
    // void update_clearsTags_whenTagListIsEmptyArray() {
    // String token = registerAndGetToken("author", "author@test.com");
    // String slug = createArticle(token).article().slug();
    //
    // ArticleResponse response = articleService.update(slug, token,
    // emptyTagListRequest());
    //
    // assertEquals(0, response.article().tagList().length);
    // }

    // Not yet implemented: tagList: null should be rejected with 422
    //
    // @Test
    // void update_throwsException_whenTagListIsNull() { ... }

    // Not yet implemented: updatedAt must change after update
    //
    // @Test
    // void update_changesUpdatedAt_afterUpdate() {
    // String token = registerAndGetToken("author", "author@test.com");
    // ArticleResponse created = createArticle(token);
    // Instant originalUpdatedAt = created.article().updatedAt();
    //
    // ArticleResponse updated = articleService.update(created.article().slug(),
    // token, ...);
    //
    // assertNotEquals(originalUpdatedAt, updated.article().updatedAt());
    // }

    // Not yet implemented: articleService.update — createdAt must be unchanged
    //
    // @Test
    // void update_preservesCreatedAt_afterUpdate() { ... }

    // Not yet implemented: articleService.update — requires valid token
    //
    // @Test
    // void update_throwsException_whenTokenIsInvalid() { ... }

    // --- Delete Article ---

    // Not yet implemented: articleService.delete(slug, token)
    //
    // @Test
    // void delete_removesArticle_fromDatabase() {
    // String token = registerAndGetToken("author", "author@test.com");
    // String slug = createArticle(token).article().slug();
    //
    // articleService.delete(slug, token);
    //
    // assertEquals(0, articleRepository.count());
    // }

    // Not yet implemented: articleService.delete(slug, token) — get after delete
    // throws
    //
    // @Test
    // void delete_throwsException_whenArticleIsRetrievedAfterDeletion() {
    // String token = registerAndGetToken("author", "author@test.com");
    // String slug = createArticle(token).article().slug();
    // articleService.delete(slug, token);
    //
    // FieldValidationException exception =
    // assertThrowsExactly(FieldValidationException.class,
    // () -> articleService.getBySlug(slug));
    // assertEquals(ErrorMessages.ARTICLE_NOT_FOUND,
    // exception.getMessagesAsString());
    // }

    // Not yet implemented: articleService.delete — requires valid token
    //
    // @Test
    // void delete_throwsException_whenTokenIsInvalid() { ... }

}
