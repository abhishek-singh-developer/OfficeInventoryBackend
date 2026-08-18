package com.inventory.officeInventory.orders.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemResponseDTO {

    private UUID id;
    private String itemName;
    private Integer quantity;
}