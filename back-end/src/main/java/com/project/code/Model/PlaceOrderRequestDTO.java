package com.project.code.Model;


import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PlaceOrderRequestDTO {
    @NotNull(message = "Store is required")
    private Long storeId;
    @NotBlank(message = "Customer name is required")
    private String customerName;
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;
    private String datetime;
    @NotEmpty(message = "At least one product is required")
    private List<@Valid PurchaseProductDTO> purchaseProduct;
    private Double totalPrice;

    // Getters and Setters

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public List<PurchaseProductDTO> getPurchaseProduct() {
        return purchaseProduct;
    }

    public void setPurchaseProduct(List<PurchaseProductDTO> purchaseProduct) {
        this.purchaseProduct = purchaseProduct;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
