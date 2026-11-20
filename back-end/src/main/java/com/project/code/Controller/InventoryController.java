package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.ProductInventoryDTO;
import com.project.code.Service.ServiceClass;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final ServiceClass service;

    public InventoryController(ServiceClass service) {
        this.service = service;
    }

    @PutMapping
    public Map<String, String> updateInventory(@Valid @RequestBody CombinedRequest request) {
        service.updateInventory(request);
        return Map.of("message", "Product and inventory updated successfully.");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> saveInventory(@Valid @RequestBody Inventory inventory) {
        service.saveInventory(inventory);
        return Map.of("message", "Inventory saved successfully.");
    }

    @GetMapping("/{storeId}")
    public Map<String, List<ProductInventoryDTO>> getAllProducts(@PathVariable long storeId) {
        return Map.of("products", service.getInventoryForStore(storeId));
    }

    @GetMapping("/filter/{category}/{name}/{storeId}")
    public Map<String, List<ProductInventoryDTO>> filterProducts(
            @PathVariable String category,
            @PathVariable String name,
            @PathVariable long storeId) {
        return Map.of("product", service.filterInventory(storeId, normalize(name), normalize(category)));
    }

    @GetMapping("/search/{name}/{storeId}")
    public Map<String, List<ProductInventoryDTO>> searchProduct(
            @PathVariable String name,
            @PathVariable long storeId) {
        return Map.of("product", service.searchInventory(name, storeId));
    }

    @DeleteMapping("/{productId}/{storeId}")
    public Map<String, String> removeProductFromStore(
            @PathVariable long productId,
            @PathVariable long storeId) {
        if (service.deleteInventoryForStore(productId, storeId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for this product and store.");
        }
        return Map.of("message", "Product removed from this store's inventory.");
    }

    @DeleteMapping("/{productId}")
    public Map<String, String> removeProductFromAllStores(@PathVariable long productId) {
        service.deleteInventoryForProduct(productId);
        return Map.of("message", "Product removed from inventory.");
    }

    @GetMapping("/validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(
            @PathVariable int quantity,
            @PathVariable long storeId,
            @PathVariable long productId) {
        return service.validateQuantity(quantity, storeId, productId);
    }

    private String normalize(String value) {
        return "null".equalsIgnoreCase(value) || value.isBlank() ? null : value;
    }
}
