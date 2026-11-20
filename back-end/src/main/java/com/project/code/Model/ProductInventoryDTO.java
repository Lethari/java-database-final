package com.project.code.Model;

import java.util.List;

public record ProductInventoryDTO(
        Long id,
        String name,
        String category,
        Double price,
        String sku,
        List<InventoryStock> inventory) {

    public record InventoryStock(Integer stockLevel) {
    }
}
