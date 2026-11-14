package com.gamerlinker.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "user_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "item_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private StoreItem item;

    @Column(name = "is_equipped")
    private Boolean isEquipped = false;

    @Column(name = "purchased_at")
    private OffsetDateTime purchasedAt = OffsetDateTime.now();

    public UserInventory() {}

    public UserInventory(UUID userId, StoreItem item, Boolean isEquipped) {
        this.userId = userId;
        this.item = item;
        this.isEquipped = isEquipped;
        this.purchasedAt = OffsetDateTime.now();
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public StoreItem getItem() { return item; }
    public void setItem(StoreItem item) { this.item = item; }

    public Boolean getIsEquipped() { return isEquipped; }
    public void setIsEquipped(Boolean isEquipped) { this.isEquipped = isEquipped; }

    public OffsetDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(OffsetDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
}