package com.gamerlinker.controller;

import com.gamerlinker.model.StoreItem;
import com.gamerlinker.model.UserInventory;
import com.gamerlinker.model.UserWallet;
import com.gamerlinker.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/items")
    public ResponseEntity<List<StoreItem>> getItems() {
        return ResponseEntity.ok(storeService.listarItens());
    }

    @GetMapping("/wallet/{userId}")
    public ResponseEntity<UserWallet> getWallet(@PathVariable UUID userId) {
        return ResponseEntity.ok(storeService.buscarOuCriarCarteira(userId));
    }

    @GetMapping("/inventory/{userId}")
    public ResponseEntity<List<UserInventory>> getInventory(@PathVariable UUID userId) {
        return ResponseEntity.ok(storeService.listarInventario(userId));
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyItem(@RequestParam UUID userId, @RequestParam UUID itemId) {
        try {
            UserInventory itemComprado = storeService.comprarItem(userId, itemId);
            return ResponseEntity.ok(itemComprado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}