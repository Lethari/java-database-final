package com.project.code.Model;

public record ProductResponseDTO(Long id, String name, String category, Double price, String sku) {
    public static ProductResponseDTO from(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getSku()
        );
    }
}
