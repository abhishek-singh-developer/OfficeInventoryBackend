package com.inventory.officeInventory.orders.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderRequest {

    private LocalDate expiryDate;

    private List<OrderItemRequest> items;
}