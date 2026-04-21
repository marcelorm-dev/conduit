package com.marcelormdev.conduit_service.article;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySlug(String articleSlug);

}
