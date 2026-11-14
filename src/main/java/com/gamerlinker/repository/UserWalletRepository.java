package com.gamerlinker.repository;

import com.gamerlinker.model.UserWallet;
import com.gamerlinker.model.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, UUID> {
    
    // Busca a carteira pelo ID do usuário
    Optional<UserWallet> findByUserId(UUID userId);
}