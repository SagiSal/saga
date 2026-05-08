package com.saga.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_inventory")
public class ProductInventory {

    @Id
    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    protected ProductInventory() {
    }

    public ProductInventory(String productId, int availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
