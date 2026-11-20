package com.project.code;

import com.project.code.Model.Inventory;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Test
    void managesInventoryAndPlacesOrderUsingPersistedProductPrice() throws Exception {
        mockMvc.perform(post("/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"North Warehouse","address":"1 Main Street"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Store created successfully."));

        long storeId = storeRepository.findAll().get(0).getId();
        mockMvc.perform(get("/store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stores[0].name").value("North Warehouse"));
        mockMvc.perform(get("/store/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("North Warehouse"));
        mockMvc.perform(get("/store/validate/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/store/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"North Warehouse Updated","address":"2 Main Street"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Store updated successfully."));

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Product","category":"Home Appliances","price":12.5,"sku":"TEST-001"}
                                """))
                .andExpect(status().isCreated());

        long productId = productRepository.findBySku("TEST-001").getId();
        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].name").value("Test Product"));
        mockMvc.perform(get("/product/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.name").value("Test Product"));
        mockMvc.perform(get("/product/searchProduct/Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].sku").value("TEST-001"));
        mockMvc.perform(get("/product/category/null/null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].id").value(productId));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":%d,"name":"Test Product","category":"Home Appliances","price":12.5,"sku":"TEST-001"}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product updated successfully."));

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"product":{"id":%d},"store":{"id":%d},"stockLevel":5}
                                """.formatted(productId, storeId)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/product/filter/{category}/{storeId}", "Home Appliances", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product[0].id").value(productId));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "product":{"id":%d,"name":"Test Product","category":"Home Appliances","price":12.5,"sku":"TEST-001"},
                                  "inventory":{"product":{"id":%d},"store":{"id":%d},"stockLevel":6}
                                }
                                """.formatted(productId, productId, storeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product and inventory updated successfully."));

        mockMvc.perform(get("/inventory/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].name").value("Test Product"))
                .andExpect(jsonPath("$.products[0].inventory[0].stockLevel").value(6));
        mockMvc.perform(get("/inventory/filter/null/null/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product[0].id").value(productId));
        mockMvc.perform(get("/inventory/search/Test/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product[0].name").value("Test Product"));

        mockMvc.perform(get("/inventory/validate/2/{storeId}/{productId}", storeId, productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));

        mockMvc.perform(post("/store/placeOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId":%d,
                                  "customerName":"Admin Customer",
                                  "customerEmail":"customer@example.com",
                                  "customerPhone":"555-0100",
                                  "datetime":"2026-11-20T13:00",
                                  "purchaseProduct":[
                                    {"id":%d,"name":"Test Product","price":0.01,"quantity":2,"total":0.02}
                                  ],
                                  "totalPrice":0.02
                                }
                                """.formatted(storeId, productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order placed successfully."));

        Inventory updatedInventory = inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId);
        org.junit.jupiter.api.Assertions.assertEquals(4, updatedInventory.getStockLevel());
        org.junit.jupiter.api.Assertions.assertEquals(25.0, orderDetailsRepository.findAll().get(0).getTotalPrice());

        mockMvc.perform(post("/store/placeOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeId":%d,
                                  "customerName":"Admin Customer",
                                  "customerEmail":"customer@example.com",
                                  "customerPhone":"555-0100",
                                  "purchaseProduct":[{"id":%d,"quantity":5}]
                                }
                                """.formatted(storeId, productId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Insufficient stock for product " + productId + "."));
        org.junit.jupiter.api.Assertions.assertEquals(
                4, inventoryRepository.findByProduct_IdAndStore_Id(productId, storeId).getStockLevel());
        org.junit.jupiter.api.Assertions.assertEquals(1, orderDetailsRepository.count());

        mockMvc.perform(delete("/inventory/{productId}/{storeId}", productId, storeId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Temporary Product","category":"Accessories","price":3.0,"sku":"TEST-DELETE"}
                                """))
                .andExpect(status().isCreated());
        long temporaryProductId = productRepository.findBySku("TEST-DELETE").getId();
        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"product":{"id":%d},"store":{"id":%d},"stockLevel":1}
                                """.formatted(temporaryProductId, storeId)))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/inventory/{productId}", temporaryProductId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/product/{id}", temporaryProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully."));

        mockMvc.perform(post("/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Temporary Store","address":"2 Main Street"}
                                """))
                .andExpect(status().isCreated());
        long temporaryStoreId = storeRepository.findAll().stream()
                .filter(store -> store.getName().equals("Temporary Store"))
                .findFirst().orElseThrow().getId();
        mockMvc.perform(delete("/store/{storeId}", temporaryStoreId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Store deleted successfully."));
    }

    @Test
    void invalidJsonUsesTheSpecifiedBadRequestResponse() throws Exception {
        mockMvc.perform(post("/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid input: The data provided is not valid."));
    }
}
