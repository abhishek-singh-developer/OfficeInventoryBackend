package com.inventory.officeInventory.orders.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectOrderRequestDTO {

    @NotBlank(message = "Rejection note is required")
    private String rejectionNote;
}