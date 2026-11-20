package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Model.ProductResponseDTO;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ServiceClass service;

    public ProductController(ServiceClass service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> addProduct(@Valid @RequestBody Product product) {
        service.saveProduct(product);
        return Map.of("message", "Product created successfully.");
    }

    @GetMapping("/product/{id}")
    public Map<String, ProductResponseDTO> getProductById(@PathVariable long id) {
        return Map.of("products", ProductResponseDTO.from(service.getProduct(id)));
    }

    @PutMapping
    public Map<String, String> updateProduct(@Valid @RequestBody Product product) {
        service.updateProduct(product);
        return Map.of("message", "Product updated successfully.");
    }

    @GetMapping("/category/{name}/{category}")
    public Map<String, List<ProductResponseDTO>> filterByNameAndCategory(
            @PathVariable String name,
            @PathVariable String category) {
        return Map.of("products", service.filterProducts(normalize(name), normalize(category)).stream()
                .map(ProductResponseDTO::from).toList());
    }

    @GetMapping
    public Map<String, List<ProductResponseDTO>> listProducts() {
        return Map.of("products", service.getProducts().stream().map(ProductResponseDTO::from).toList());
    }

    @GetMapping("/filter/{category}/{storeId}")
    public Map<String, List<ProductResponseDTO>> getProductsByCategoryAndStore(
            @PathVariable String category,
            @PathVariable long storeId) {
        return Map.of("product", service.getProductsByStoreAndCategory(storeId, normalize(category)).stream()
                .map(ProductResponseDTO::from).toList());
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable long id) {
        service.deleteProduct(id);
        return Map.of("message", "Product deleted successfully.");
    }

    @GetMapping("/searchProduct/{name}")
    public Map<String, List<ProductResponseDTO>> searchProduct(@PathVariable String name) {
        return Map.of("products", service.searchProducts(name).stream().map(ProductResponseDTO::from).toList());
    }

    private String normalize(String value) {
        return "null".equalsIgnoreCase(value) || value.isBlank() ? null : value;
    }
}
