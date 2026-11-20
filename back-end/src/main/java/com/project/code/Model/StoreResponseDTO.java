package com.project.code.Model;

public record StoreResponseDTO(Long id, String name, String address) {
    public static StoreResponseDTO from(Store store) {
        return new StoreResponseDTO(store.getId(), store.getName(), store.getAddress());
    }
}
