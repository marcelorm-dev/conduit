package com.marcelormdev.conduit_service.article;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.profile.ProfileRepository;
import com.marcelormdev.conduit_service.user.UserRepository;
import com.marcelormdev.conduit_service.user.UserService;
import com.marcelormdev.conduit_service.user.UserServiceTestHelper;

@SpringBootTest
class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserService userService;

    private UserServiceTestHelper userServiceHelper;

    @BeforeEach
    void beforeEachTest() {
        articleRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        userServiceHelper = new UserServiceTestHelper(userService);
    }

    private String registerUserAndGetToken(String username, String email) {
        return userServiceHelper.registerUser(username, email, "password123").user().token();
    }

    private ArticleResponse createArticle(String token) {
        return articleService.create(token, new CreateArticleRequest(
                "Test Article",
                "Test description",
                "Test body content",
                new String[] { "tag1", "tag2" }));
    }

    // --- Create Article ---

    @Test
    void create_returnsArticle_whenInputIsValid() {
        String token = registerUserAndGetToken("author", "author@test.com");

        ArticleResponse response = createArticle(token);

        assertEquals("Test Article", response.article().title());
        assertEquals("Test description", response.article().description());
        assertEquals("Test body content", response.article().body());
        assertArrayEquals(new String[] { "tag1", "tag2" }, response.article().tagList());
        assertNull(response.article().slug());
        assertNotNull(response.article().createdAt());
        assertFalse(response.article().favorited());
        assertEquals(0, response.article().favoritesCount());
        assertEquals("author", response.article().author().username());
    }

    @Test
    void create_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String token : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> createArticle(token));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void create_throwsException_whenTokenIsInvalid() {
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> createArticle("invalid-token"));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void create_throwsException_whenTitleIsNullOrBlank() {
        String token = registerUserAndGetToken("author", "author@test.com");

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
        String token = registerUserAndGetToken("author", "author@test.com");

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
        String token = registerUserAndGetToken("author", "author@test.com");

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
        String token = registerUserAndGetToken("author", "author@test.com");

        ArticleResponse response = articleService.create(token, new CreateArticleRequest(
                "No Tags Article", "desc", "body", new String[] {}));

        assertEquals(0, response.article().tagList().length);
    }

    // --- Get Article by slug ---

    // Not yet implemented: articleService.getBySlug(slug)
    //
    // @Test
    // void getBySlug_returnsArticle_whenSlugExists() {
    // String token = registerAndGetToken("author", "author@test.com");
    // ArticleResponse created = createArticle(token);
    // String slug = created.article().slug();
    //
    // ArticleResponse response = articleService.getBySlug(slug);
    //
    // assertEquals("Test Article", response.article().title());
    // assertEquals(slug, response.article().slug());
    // assertEquals("Test description", response.article().description());
    // assertEquals("Test body content", response.article().body());
    // assertEquals(2, response.article().tagList().length);
    // assertFalse(response.article().favorited());
    // assertEquals(0, response.article().favoritesCount());
    // assertEquals("author", response.article().author().username());
    // }

    // Not yet implemented: articleService.getBySlug(slug) — expected to throw when
    // not found
    //
    // @Test
    // void getBySlug_throwsException_whenSlugNotFound() {
    // FieldValidationException exception =
    // assertThrowsExactly(FieldValidationException.class,
    // () -> articleService.getBySlug("non-existent-slug"));
    // assertEquals(ErrorMessages.ARTICLE_NOT_FOUND,
    // exception.getMessagesAsString());
    // }

    // --- List Articles ---

    // Not yet implemented: articleService.list(author, tag, token)
    //
    // @Test
    // void list_returnsAllArticles_withoutFilters() {
    // String token = registerAndGetToken("author", "author@test.com");
    // createArticle(token);
    //
    // List<ArticleResponse> articles = articleService.list(null, null, null);
    //
    // assertFalse(articles.isEmpty());
    // }

    // Not yet implemented: articleService.list(author, tag, token)
    //
    // @Test
    // void list_filtersByAuthor() {
    // String token1 = registerAndGetToken("author1", "author1@test.com");
    // String token2 = registerAndGetToken("author2", "author2@test.com");
    // createArticle(token1);
    // createArticle(token2);
    //
    // List<ArticleResponse> articles = articleService.list("author1", null, null);
    //
    // assertEquals(1, articles.size());
    // assertEquals("author1", articles.get(0).article().author().username());
    // }

    // Not yet implemented: articleService.list(author, tag, token)
    //
    // @Test
    // void list_filtersByTag() {
    // String token = registerAndGetToken("author", "author@test.com");
    // articleService.create(token, new CreateArticleRequest("A", "d", "b", new
    // String[]{"java"}));
    // articleService.create(token, new CreateArticleRequest("B", "d", "b", new
    // String[]{"python"}));
    //
    // List<ArticleResponse> articles = articleService.list(null, "java", null);
    //
    // assertEquals(1, articles.size());
    // }

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
