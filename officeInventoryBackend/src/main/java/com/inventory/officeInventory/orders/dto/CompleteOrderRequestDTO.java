package com.inventory.officeInventory.orders.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteOrderRequestDTO {

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;
}