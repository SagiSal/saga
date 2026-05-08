package com.saga.inventory.repository;

import com.saga.inventory.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<ProductInventory, String> {
}
