package com.gamerlinker.service;

import com.gamerlinker.model.StoreItem;
import com.gamerlinker.model.UserInventory;
import com.gamerlinker.model.UserWallet;
import com.gamerlinker.repository.StoreItemRepository;
import com.gamerlinker.repository.UserInventoryRepository;
import com.gamerlinker.repository.UserWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private final UserWalletRepository walletRepository;
    private final StoreItemRepository itemRepository;
    private final UserInventoryRepository inventoryRepository;

    public StoreService(UserWalletRepository walletRepository, 
                        StoreItemRepository itemRepository, 
                        UserInventoryRepository inventoryRepository) {
        this.walletRepository = walletRepository;
        this.itemRepository = itemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<StoreItem> listarItens() {
        return itemRepository.findAll();
    }

    public List<UserInventory> listarInventario(UUID userId) {
        return inventoryRepository.findByUserId(userId);
    }

    public UserWallet buscarOuCriarCarteira(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new UserWallet(userId, 0)));
    }

    @Transactional
    public UserInventory comprarItem(UUID userId, UUID itemId) {
        StoreItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        if (inventoryRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new RuntimeException("Você já possui este item!");
        }

        UserWallet wallet = buscarOuCriarCarteira(userId);

        if (wallet.getCoins() < item.getPrice()) {
            throw new RuntimeException("Moedas insuficientes!");
        }

        wallet.setCoins(wallet.getCoins() - item.getPrice());
        walletRepository.save(wallet);

        UserInventory novoItem = new UserInventory(userId, item, false);
        return inventoryRepository.save(novoItem);
    }
}