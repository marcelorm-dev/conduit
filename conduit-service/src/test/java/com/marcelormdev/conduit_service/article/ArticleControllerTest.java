package com.marcelormdev.conduit_service.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.marcelormdev.conduit_service.apitest.ArticleRestCaller;
import com.marcelormdev.conduit_service.apitest.ControllerTest;
import com.marcelormdev.conduit_service.apitest.UserRestCaller;
import com.marcelormdev.conduit_service.user.UserRepository;

class ArticleControllerTest extends ControllerTest {

    @Autowired
    private UserRepository userRepository;

    private UserRestCaller userRestCaller;
    private ArticleRestCaller articleRestCaller;

    @BeforeEach
    void beforeEachTest() {
        userRepository.deleteAll();
        userRestCaller = new UserRestCaller(restClient);
        articleRestCaller = new ArticleRestCaller(restClient);
    }

    private void registerUser(String username, String email) {
        userRestCaller.callRegisterAPI("""
                        {
                            "user": {
                                "username": "%s",
                                "email": "%s",
                                "password": "password123"
                            }
                        }
                """.formatted(username, email))
                .expectStatus().isCreated();
    }

    @Test
    void createArticle_returnsArticleData_whenInputIsValid() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": ["tag1", "tag2"]
                            }
                        }
                """)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.article.title").isEqualTo("Test Article")
                .jsonPath("$.article.slug").isNotEmpty()
                .jsonPath("$.article.description").isEqualTo("Test description")
                .jsonPath("$.article.body").isEqualTo("Test body content")
                .jsonPath("$.article.tagList[0]").isEqualTo("tag1")
                .jsonPath("$.article.tagList[1]").isEqualTo("tag2")
                .jsonPath("$.article.createdAt").isNotEmpty()
                .jsonPath("$.article.favorited").isEqualTo(false)
                .jsonPath("$.article.favoritesCount").isEqualTo(0)
                .jsonPath("$.article.author.username").isEqualTo("author");
    }

    @Test
    void createArticle_returns401_whenTokenIsMissing() {
        articleRestCaller.callCreateArticleAPI(null, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isUnauthorized();
    }

    @Test
    void getArticle_returnsArticle_whenSlugExists() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": ["tag1", "tag2"]
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callGetArticleAPI("test-article")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.article.title").isEqualTo("Test Article")
                .jsonPath("$.article.slug").isEqualTo("test-article")
                .jsonPath("$.article.description").isEqualTo("Test description")
                .jsonPath("$.article.body").isEqualTo("Test body content")
                .jsonPath("$.article.tagList").isArray()
                .jsonPath("$.article.createdAt").isNotEmpty()
                .jsonPath("$.article.favorited").isEqualTo(false)
                .jsonPath("$.article.favoritesCount").isEqualTo(0)
                .jsonPath("$.article.author.username").isEqualTo("author");
    }

    @Test
    void getArticle_returns404_whenSlugNotFound() {
        articleRestCaller.callGetArticleAPI("non-existent-slug")
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errors.article[0]").isEqualTo("not found");
    }

    @Test
    void favoriteArticle_returnsArticle_withFavoritedTrue_whenTokenIsValid() {
        registerUser("author", "author@test.com");
        String authorToken = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(authorToken, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        registerUser("reader", "reader@test.com");
        String readerToken = authService.generateToken("reader@test.com");

        articleRestCaller.callFavoriteArticleAPI("test-article", readerToken)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.article.slug").isEqualTo("test-article")
                .jsonPath("$.article.favorited").isEqualTo(true)
                .jsonPath("$.article.favoritesCount").isEqualTo(1);
    }

    @Test
    void favoriteArticle_returns401_whenTokenIsMissing() {
        articleRestCaller.callFavoriteArticleAPI("test-article", null)
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void favoriteArticle_returns404_whenSlugNotFound() {
        registerUser("reader", "reader@test.com");
        String readerToken = authService.generateToken("reader@test.com");

        articleRestCaller.callFavoriteArticleAPI("non-existent-slug", readerToken)
                .expectStatus().isNotFound();
    }

    @Test
    void unfavoriteArticle_returnsArticle_withFavoritedFalse_whenTokenIsValid() {
        registerUser("author", "author@test.com");
        String authorToken = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(authorToken, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        registerUser("reader", "reader@test.com");
        String readerToken = authService.generateToken("reader@test.com");

        articleRestCaller.callFavoriteArticleAPI("test-article", readerToken)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.article.slug").isEqualTo("test-article")
                .jsonPath("$.article.favorited").isEqualTo(true)
                .jsonPath("$.article.favoritesCount").isEqualTo(1);

        articleRestCaller.callUnfavoriteArticleAPI("test-article", readerToken)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.article.slug").isEqualTo("test-article")
                .jsonPath("$.article.favorited").isEqualTo(false)
                .jsonPath("$.article.favoritesCount").isEqualTo(0);
    }

    @Test
    void unfavoriteArticle_returns401_whenTokenIsMissing() {
        articleRestCaller.callUnfavoriteArticleAPI("test-article", null)
                .expectStatus().isUnauthorized();
    }

    @Test
    void unfavoriteArticle_returns404_whenSlugNotFound() {
        registerUser("reader", "reader@test.com");
        String readerToken = authService.generateToken("reader@test.com");

        articleRestCaller.callUnfavoriteArticleAPI("non-existent-slug", readerToken)
                .expectStatus().isNotFound();
    }

    // --- List Articles ---

    @Test
    void listArticles_returnsArticleList_withoutAuth() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": ["tag1", "tag2"]
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callListArticlesAPI()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.articles").isArray()
                .jsonPath("$.articlesCount").isEqualTo(1)
                .jsonPath("$.articles[0].title").isEqualTo("Test Article")
                .jsonPath("$.articles[0].slug").isEqualTo("test-article")
                .jsonPath("$.articles[0].author.username").isEqualTo("author");
    }

    @Test
    void listArticles_returnsEmptyList_whenNoArticlesExist() {
        articleRestCaller.callListArticlesAPI()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.articles").isArray()
                .jsonPath("$.articlesCount").isEqualTo(0);
    }

    @Test
    void listArticles_returnsFavoritedTrue_whenAuthenticatedUserFavoritedArticle() {
        registerUser("author", "author@test.com");
        String authorToken = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(authorToken, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        registerUser("reader", "reader@test.com");
        String readerToken = authService.generateToken("reader@test.com");
        articleRestCaller.callFavoriteArticleAPI("test-article", readerToken);

        articleRestCaller.callListArticlesAPI(readerToken)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.articles[0].favorited").isEqualTo(true)
                .jsonPath("$.articles[0].favoritesCount").isEqualTo(1);
    }

    @Test
    void listArticles_filtersByAuthor() {
        registerUser("author1", "author1@test.com");
        String token1 = authService.generateToken("author1@test.com");
        articleRestCaller.callCreateArticleAPI(token1, """
                        {
                            "article": {
                                "title": "Article by Author 1",
                                "description": "desc",
                                "body": "body",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        registerUser("author2", "author2@test.com");
        String token2 = authService.generateToken("author2@test.com");
        articleRestCaller.callCreateArticleAPI(token2, """
                        {
                            "article": {
                                "title": "Article by Author 2",
                                "description": "desc",
                                "body": "body",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callListArticlesAPI("author=author1", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.articlesCount").isEqualTo(1)
                .jsonPath("$.articles[0].author.username").isEqualTo("author1");
    }

    @Test
    void listArticles_filtersByTag() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Java Article",
                                "description": "desc",
                                "body": "body",
                                "tagList": ["java"]
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Spring Article",
                                "description": "desc",
                                "body": "body",
                                "tagList": ["spring"]
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callListArticlesAPI("tag=java", null)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.articlesCount").isEqualTo(1)
                .jsonPath("$.articles[0].title").isEqualTo("Java Article");
    }

    @Test
    void listArticles_filtersByFavorited() {
        registerUser("author", "author@test.com");
        String authorToken = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(authorToken, """
                        {
                            "article": {
                                "title": "Favorited Article",
                                "description": "desc",
                                "body": "body",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callCreateArticleAPI(authorToken, """
                        {
                            "article": {
                                "title": "Not Favorited Article",
                                "description": "desc",
                                "body": "body",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        registerUser("reader", "reader@test.com");
        String readerToken = authService.generateToken("reader@test.com");
        articleRestCaller.callFavoriteArticleAPI("favorited-article", readerToken);

        articleRestCaller.callListArticlesAPI("favorited=true", readerToken)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.articlesCount").isEqualTo(1)
                .jsonPath("$.articles[0].title").isEqualTo("Favorited Article")
                .jsonPath("$.articles[0].favorited").isEqualTo(true);
    }

    // --- Update Article ---

    @Test
    void updateArticle_returnsUpdatedArticle_whenTokenIsValid() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callUpdateArticleAPI("test-article", token, """
                        {
                            "article": {
                                "body": "Updated body content"
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.article.body").isEqualTo("Updated body content")
                .jsonPath("$.article.title").isEqualTo("Test Article")
                .jsonPath("$.article.tagList").isArray();
    }

    @Test
    void updateArticle_returns401_whenTokenIsMissing() {
        articleRestCaller.callUpdateArticleAPI("some-slug", null, """
                        {
                            "article": {
                                "body": "Updated body content"
                            }
                        }
                """)
                .expectStatus().isUnauthorized();
    }

    // --- Delete Article ---

    @Test
    void deleteArticle_returns204_whenAuthorDeletesOwnArticle() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(token, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        articleRestCaller.callDeleteArticleAPI("test-article", token)
                .expectStatus().isNoContent();

        articleRestCaller.callGetArticleAPI("test-article")
                .expectStatus().isNotFound();
    }

    @Test
    void deleteArticle_returns401_whenTokenIsMissing() {
        articleRestCaller.callDeleteArticleAPI("some-slug", null)
                .expectStatus().isUnauthorized();
    }

    @Test
    void deleteArticle_returns404_whenSlugNotFound() {
        registerUser("author", "author@test.com");
        String token = authService.generateToken("author@test.com");

        articleRestCaller.callDeleteArticleAPI("non-existent-slug", token)
                .expectStatus().isNotFound();
    }

    @Test
    void deleteArticle_returns403_whenUserIsNotAuthor() {
        registerUser("author", "author@test.com");
        String authorToken = authService.generateToken("author@test.com");

        articleRestCaller.callCreateArticleAPI(authorToken, """
                        {
                            "article": {
                                "title": "Test Article",
                                "description": "Test description",
                                "body": "Test body content",
                                "tagList": []
                            }
                        }
                """)
                .expectStatus().isCreated();

        registerUser("other", "other@test.com");
        String otherToken = authService.generateToken("other@test.com");

        articleRestCaller.callDeleteArticleAPI("test-article", otherToken)
                .expectStatus().isForbidden();
    }

}
