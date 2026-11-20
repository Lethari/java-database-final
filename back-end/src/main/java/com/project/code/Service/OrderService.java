package com.project.code.Service;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Product;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(
            CustomerRepository customerRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            OrderDetailsRepository orderDetailsRepository,
            OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public void saveOrder(PlaceOrderRequestDTO request) {
        if (request == null || request.getStoreId() == null
                || isBlank(request.getCustomerName()) || isBlank(request.getCustomerEmail())
                || isBlank(request.getCustomerPhone()) || request.getPurchaseProduct() == null
                || request.getPurchaseProduct().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Store, customer details, and at least one product are required.");
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found."));
        Customer customer = customerRepository.findByEmail(request.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setName(request.getCustomerName());
            customer.setEmail(request.getCustomerEmail());
            customer.setPhone(request.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        LocalDateTime orderDate = parseOrderDate(request.getDatetime());
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (PurchaseProductDTO purchase : request.getPurchaseProduct()) {
            if (purchase == null || purchase.getId() == null || purchase.getQuantity() == null
                    || purchase.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each order item must include a product ID and a positive quantity.");
            }

            Product product = productRepository.findById(purchase.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Product " + purchase.getId() + " not found."));
            Inventory inventory = inventoryRepository.findForUpdateByProduct_IdAndStore_Id(product.getId(), store.getId());
            if (inventory == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Product " + product.getId() + " is not stocked at this store.");
            }
            if (inventory.getStockLevel() == null || inventory.getStockLevel() < purchase.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Insufficient stock for product " + product.getId() + ".");
            }
            if (product.getPrice() == null || product.getPrice() < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Product " + product.getId() + " has an invalid price.");
            }

            inventory.setStockLevel(inventory.getStockLevel() - purchase.getQuantity());
            inventoryRepository.save(inventory);

            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(purchase.getQuantity())));
            items.add(new OrderItem(null, product, purchase.getQuantity(), product.getPrice()));
        }

        OrderDetails order = new OrderDetails(customer, store, total.doubleValue(), orderDate);
        orderDetailsRepository.save(order);
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setOrderItems(items);
        orderItemRepository.saveAll(items);
    }

    private LocalDateTime parseOrderDate(String value) {
        if (isBlank(value)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order date must use ISO local date-time format.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
