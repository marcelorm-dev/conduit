package com.marcelormdev.conduit_service.article;

import java.time.Instant;
import java.util.Objects;

import com.marcelormdev.conduit_service.profile.Profile;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "articles")
class Article {

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    private String slug;
    private String title;
    private String description;
    private String body;
    private String[] tagList;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean favorited;
    private Integer favoritesCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    Article() {
        this.createdAt = Instant.now();
    }

    Article(String title, String description, String body, String[] tagList, Profile profile) {
        this();
        this.title = title;
        this.description = description;
        this.body = body;
        this.tagList = tagList;
        this.profile = profile;
        this.favorited = false;
        this.favoritesCount = 0;
    }

    String getSlug() {
        return slug;
    }

    void setSlug(String slug) {
        this.slug = slug;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getBody() {
        return body;
    }

    void setBody(String body) {
        this.body = body;
    }

    String[] getTagList() {
        return tagList;
    }

    void setTagList(String[] tagList) {
        this.tagList = tagList;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    Boolean isFavorited() {
        return favorited;
    }

    void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    Integer getFavoritesCount() {
        return favoritesCount;
    }

    void addFavorites() {
        this.favoritesCount = favoritesCount + 1;
    }

    void subtractFavorites() {
        if (this.favoritesCount > 0)
            this.favoritesCount = favoritesCount - 1;
    }

    Profile getProfile() {
        return profile;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Article article))
            return false;
        return Objects.equals(this.id, article.id);
    }

}
