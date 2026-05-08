package com.saga.inventory;

import com.saga.inventory.repository.InventoryRepository;
import com.saga.inventory.model.ProductInventory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner inventorySeeder(InventoryRepository repository) {
        return args -> {
            repository.save(new ProductInventory("product-1", 50));
            repository.save(new ProductInventory("product-2", 40));
            repository.save(new ProductInventory("product-3", 30));
        };
    }
}
