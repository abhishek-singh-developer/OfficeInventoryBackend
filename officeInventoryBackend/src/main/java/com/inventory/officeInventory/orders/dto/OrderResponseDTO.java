package com.inventory.officeInventory.orders.dto;

import com.inventory.officeInventory.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDTO {

    private UUID id;
    private String orderNo;
    private OrderStatus status;
    private LocalDate expiryDate;
    private UserResponseDTO createdBy;
    private List<OrderItemResponseDTO> items;
    private String transactionReference;
    private String rejectionNote;
}