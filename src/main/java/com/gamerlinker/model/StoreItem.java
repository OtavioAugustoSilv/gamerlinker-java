package com.gamerlinker.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "store_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category; // AVATAR_FRAME, PROFILE_THEME, CHAT_BUBBLE

    @Column(nullable = false)
    private Integer price;

    @Column(name = "required_rank", length = 20)
    private String requiredRank = "BRONZE";

    @Column(name = "asset_url", nullable = false, columnDefinition = "TEXT")
    private String assetUrl;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public StoreItem() {}

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getRequiredRank() { return requiredRank; }
    public void setRequiredRank(String requiredRank) { this.requiredRank = requiredRank; }

    public String getAssetUrl() { return assetUrl; }
    public void setAssetUrl(String assetUrl) { this.assetUrl = assetUrl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}