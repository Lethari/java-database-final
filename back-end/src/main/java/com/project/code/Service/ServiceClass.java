package com.project.code.Service;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Model.ProductInventoryDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ServiceClass {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public ServiceClass(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            StoreRepository storeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    public boolean validateInventory(Inventory inventory) {
        Long productId = inventory == null || inventory.getProduct() == null
                ? null : inventory.getProduct().getId();
        Long storeId = inventory == null || inventory.getStore() == null
                ? null : inventory.getStore().getId();
        if (productId == null || storeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product and store are required.");
        }
        return inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId) == null;
    }

    public boolean validateProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().isBlank()
                || product.getSku() == null || product.getSku().isBlank()) {
            return false;
        }
        return productRepository.findByName(product.getName()) == null
                && productRepository.findBySku(product.getSku()) == null;
    }

    public boolean ValidateProductId(long id) {
        return productRepository.existsById(id);
    }

    public Inventory getInventoryId(Inventory inventory) {
        Long productId = inventory == null || inventory.getProduct() == null
                ? null : inventory.getProduct().getId();
        Long storeId = inventory == null || inventory.getStore() == null
                ? null : inventory.getStore().getId();
        if (productId == null || storeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product and store are required.");
        }
        return inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId);
    }

    @Transactional(readOnly = true)
    public List<Store> getStores() {
        return storeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Store getStore(long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found."));
    }

    @Transactional(readOnly = true)
    public boolean storeExists(long storeId) {
        return storeRepository.existsById(storeId);
    }

    @Transactional
    public Store saveStore(Store store) {
        if (store.getId() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new store must not include an ID.");
        }
        return storeRepository.save(store);
    }

    @Transactional
    public Store updateStore(long storeId, Store changes) {
        Store store = getStore(storeId);
        store.setName(changes.getName());
        store.setAddress(changes.getAddress());
        return storeRepository.save(store);
    }

    @Transactional
    public void deleteStore(long storeId) {
        storeRepository.delete(getStore(storeId));
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProduct(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
    }

    @Transactional
    public Product saveProduct(Product product) {
        if (product.getId() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new product must not include an ID.");
        }
        if (!validateProduct(product)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this name or SKU already exists.");
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Product changes) {
        if (changes.getId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A product ID is required.");
        }
        Product product = getProduct(changes.getId());
        ensureProductUnique(changes, product.getId());
        product.setName(changes.getName());
        product.setCategory(changes.getCategory());
        product.setPrice(changes.getPrice());
        product.setSku(changes.getSku());
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> filterProducts(String name, String category) {
        return productRepository.findByOptionalNameAndCategory(name, category);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByStoreAndCategory(long storeId, String category) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found.");
        }
        if (category == null) {
            return productRepository.findProductsByStoreId(storeId);
        }
        return productRepository.findProductByCategory(storeId, category);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String name) {
        return productRepository.findProductBySubName(name);
    }

    @Transactional
    public void deleteProduct(long productId) {
        Product product = getProduct(productId);
        inventoryRepository.deleteByProductId(productId);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductInventoryDTO> getInventoryForStore(long storeId) {
        requireStore(storeId);
        return inventoryRepository.findByStore_Id(storeId).stream()
                .map(this::toProductInventory)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductInventoryDTO> filterInventory(long storeId, String name, String category) {
        return getInventoryForStore(storeId).stream()
                .filter(item -> name == null || (item.name() != null
                        && item.name().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))))
                .filter(item -> category == null || item.category().equalsIgnoreCase(category))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductInventoryDTO> searchInventory(String name, long storeId) {
        return filterInventory(storeId, name, null);
    }

    @Transactional
    public Inventory saveInventory(Inventory request) {
        Long productId = request == null || request.getProduct() == null
                ? null : request.getProduct().getId();
        Long storeId = request == null || request.getStore() == null
                ? null : request.getStore().getId();
        if (request.getId() != null || productId == null || storeId == null || request.getStockLevel() == null
                || request.getStockLevel() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product, store, and a non-negative stock level are required.");
        }
        if (!validateInventory(request)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory already exists for this product and store.");
        }

        Product product = getProduct(productId);
        Store store = getStore(storeId);
        request.setProduct(product);
        request.setStore(store);
        return inventoryRepository.save(request);
    }

    @Transactional
    public Inventory updateInventory(CombinedRequest request) {
        if (request == null || request.getProduct() == null || request.getInventory() == null
                || request.getProduct().getId() <= 0
                || request.getInventory().getStore() == null
                || request.getInventory().getStore().getId() <= 0
                || request.getInventory().getStockLevel() == null
                || request.getInventory().getStockLevel() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete product and inventory data are required.");
        }

        Product changes = request.getProduct();
        Product product = getProduct(changes.getId());
        ensureProductUnique(changes, product.getId());
        product.setName(changes.getName());
        product.setCategory(changes.getCategory());
        product.setPrice(changes.getPrice());
        product.setSku(changes.getSku());
        productRepository.save(product);

        Store store = getStore(request.getInventory().getStore().getId());
        Inventory inventory = inventoryRepository.findByProduct_IdAndStore_Id(product.getId(), store.getId());
        if (inventory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for this product and store.");
        }
        inventory.setStockLevel(request.getInventory().getStockLevel());
        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public boolean validateQuantity(int quantity, long storeId, long productId) {
        if (quantity <= 0) {
            return false;
        }
        Inventory inventory = inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId);
        return inventory != null && inventory.getStockLevel() != null
                && inventory.getStockLevel() >= quantity;
    }

    @Transactional
    public int deleteInventoryForStore(long productId, long storeId) {
        requireStore(storeId);
        return inventoryRepository.deleteByProductAndStore(productId, storeId);
    }

    @Transactional
    public void deleteInventoryForProduct(long productId) {
        if (inventoryRepository.deleteByProductId(productId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for this product.");
        }
    }

    private void ensureProductUnique(Product changes, long existingId) {
        if (changes.getName() == null || changes.getName().isBlank()
                || changes.getSku() == null || changes.getSku().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name and SKU are required.");
        }
        Product named = productRepository.findByName(changes.getName());
        Product sku = productRepository.findBySku(changes.getSku());
        if ((named != null && !Objects.equals(named.getId(), existingId))
                || (sku != null && !Objects.equals(sku.getId(), existingId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this name or SKU already exists.");
        }
    }

    private void requireStore(long storeId) {
        if (!storeExists(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found.");
        }
    }

    private ProductInventoryDTO toProductInventory(Inventory inventory) {
        Product product = inventory.getProduct();
        return new ProductInventoryDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getSku(),
                List.of(new ProductInventoryDTO.InventoryStock(inventory.getStockLevel()))
        );
    }
}
