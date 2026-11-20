package com.project.code.Repo;

import com.project.code.Model.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category
    List<Product> findByCategory(String category);

    // Find products within a price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Find product by SKU
    Product findBySku(String sku);

    // Find product by name
    Product findByName(String name);


    // Find products by name pattern for a specific store
    @Query("SELECT DISTINCT i.product FROM Inventory i " +
           "WHERE i.store.id = :storeId " +
           "AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :pname, '%'))")
    List<Product> findByNameLike(
            @Param("storeId") Long storeId,
            @Param("pname") String pname
    );


    // Find products by name and category for a specific store
    @Query("SELECT DISTINCT i.product FROM Inventory i " +
           "WHERE i.store.id = :storeId " +
           "AND LOWER(i.product.name) LIKE LOWER(CONCAT('%', :pname, '%')) " +
           "AND i.product.category = :category")
    List<Product> findByNameAndCategory(
            @Param("storeId") Long storeId,
            @Param("pname") String pname,
            @Param("category") String category
    );


    // Find products by category for a specific store
    @Query("SELECT DISTINCT i.product FROM Inventory i " +
           "WHERE i.store.id = :storeId " +
           "AND i.product.category = :category")
    List<Product> findByCategoryAndStoreId(
            @Param("storeId") Long storeId,
            @Param("category") String category
    );


    // Find products by partial name (case-insensitive)
    @Query("SELECT p FROM Product p " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :pname, '%'))")
    List<Product> findProductBySubName(
            @Param("pname") String pname
    );


    // Find all products for a specific store
    @Query("SELECT DISTINCT i.product FROM Inventory i " +
           "WHERE i.store.id = :storeId")
    List<Product> findProductsByStoreId(
            @Param("storeId") Long storeId
    );


    // Find products by category for a specific store
    @Query("SELECT DISTINCT i.product FROM Inventory i " +
           "WHERE i.store.id = :storeId " +
           "AND i.product.category = :category")
    List<Product> findProductByCategory(
            @Param("storeId") Long storeId,
            @Param("category") String category
    );


    // Find products by partial name and category
    @Query("SELECT p FROM Product p " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :pname, '%')) " +
           "AND p.category = :category")
    List<Product> findProductBySubNameAndCategory(
            @Param("pname") String pname,
            @Param("category") String category
    );

    @Query("SELECT p FROM Product p " +
           "WHERE (:pname IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :pname, '%'))) " +
           "AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))")
    List<Product> findByOptionalNameAndCategory(
            @Param("pname") String pname,
            @Param("category") String category
    );

}
