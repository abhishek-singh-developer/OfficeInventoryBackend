package com.inventory.officeInventory.orders.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    private String itemName;

    private Integer quantity;
}