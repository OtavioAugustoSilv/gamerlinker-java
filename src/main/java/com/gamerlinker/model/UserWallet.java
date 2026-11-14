package com.gamerlinker.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_wallets")
public class UserWallet {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private Integer coins = 0;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UserWallet() {}

    public UserWallet(UUID userId, Integer coins) {
        this.userId = userId;
        this.coins = coins;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters e Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getCoins() { return coins; }
    public void setCoins(Integer coins) { 
        this.coins = coins; 
        this.updatedAt = OffsetDateTime.now();
    }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}