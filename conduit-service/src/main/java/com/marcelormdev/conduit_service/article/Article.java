package com.marcelormdev.conduit_service.article;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.marcelormdev.conduit_service.profile.Profile;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_favorites", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "profile_id"))
    private Set<Profile> favoritedBy;

    Article() {
        this.createdAt = Instant.now();
        this.favoritedBy = new HashSet<>();
    }

    Article(String title, String description, String body, String[] tagList, Profile profile) {
        this();
        this.title = title;
        this.description = description;
        this.body = body;
        this.tagList = tagList;
        this.author = profile;
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

    Boolean isFavoritedBy(Profile profile) {
        return favoritedBy.contains(profile);
    }

    void addFavorited(Profile profile) {
        this.favoritedBy.add(profile);
    }

    Integer getFavoritesCount() {
        return this.favoritedBy.size();
    }

    Profile getAuthor() {
        return author;
    }

    boolean isAuthoredBy(String username) {
        return username != null && !username.isBlank() && this.author.getUsername().equals(username);
    }

    boolean hasTag(String tag) {
        return tag != null && !tag.isBlank() && Arrays.asList(this.tagList).contains(tag);
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
