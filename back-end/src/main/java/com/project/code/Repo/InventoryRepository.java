package com.project.code.Repo;

import com.project.code.Model.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Find inventory by Product ID and Store ID
    Inventory findByProduct_IdAndStore_Id(Long productId, Long storeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Inventory findForUpdateByProduct_IdAndStore_Id(Long productId, Long storeId);

    // Find all inventory records for a specific Store
    List<Inventory> findByStore_Id(Long storeId);

    // Delete inventory records associated with a Product ID
    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.product.id = :productId")
    int deleteByProductId(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId")
    int deleteByProductAndStore(
            @Param("productId") Long productId,
            @Param("storeId") Long storeId
    );

}
