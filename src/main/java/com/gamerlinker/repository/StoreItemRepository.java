package com.gamerlinker.repository;

import com.gamerlinker.model.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, UUID> {

    // Lista os itens da loja filtrados por categoria (ex: AVATAR_FRAME, PROFILE_THEME)
    List<StoreItem> findByCategory(String category);

    // Lista os itens disponíveis para o Rank do usuário
    List<StoreItem> findByRequiredRank(String requiredRank);
}