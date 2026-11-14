package com.gamerlinker.repository;

import com.gamerlinker.model.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, UUID> {

    // Lista todos os itens comprados por um usuário
    List<UserInventory> findByUserId(UUID userId);

    // Verifica se o usuário já possui um item específico (evita compra duplicada)
    boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

    // Busca os itens atualmente equipados pelo usuário
    List<UserInventory> findByUserIdAndIsEquippedTrue(UUID userId);

    // Busca um item específico no inventário do usuário para equipar/desequipar
    Optional<UserInventory> findByUserIdAndItemId(UUID userId, UUID itemId);
}