package com.project.code.Controller;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Store;
import com.project.code.Model.StoreResponseDTO;
import com.project.code.Service.OrderService;
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
@RequestMapping("/store")
public class StoreController {

    private final ServiceClass service;
    private final OrderService orderService;

    public StoreController(ServiceClass service, OrderService orderService) {
        this.service = service;
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> addStore(@Valid @RequestBody Store store) {
        service.saveStore(store);
        return Map.of("message", "Store created successfully.");
    }

    @GetMapping
    public Map<String, List<StoreResponseDTO>> listStores() {
        return Map.of("stores", service.getStores().stream().map(StoreResponseDTO::from).toList());
    }

    @GetMapping("/{storeId}")
    public StoreResponseDTO getStore(@PathVariable long storeId) {
        return StoreResponseDTO.from(service.getStore(storeId));
    }

    @GetMapping("/validate/{storeId}")
    public boolean validateStore(@PathVariable long storeId) {
        return service.storeExists(storeId);
    }

    @PutMapping("/{storeId}")
    public Map<String, String> updateStore(
            @PathVariable long storeId,
            @Valid @RequestBody Store store) {
        service.updateStore(storeId, store);
        return Map.of("message", "Store updated successfully.");
    }

    @DeleteMapping("/{storeId}")
    public Map<String, String> deleteStore(@PathVariable long storeId) {
        service.deleteStore(storeId);
        return Map.of("message", "Store deleted successfully.");
    }

    @PostMapping("/placeOrder")
    public Map<String, String> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO request) {
        orderService.saveOrder(request);
        return Map.of("message", "Order placed successfully.");
    }
}
