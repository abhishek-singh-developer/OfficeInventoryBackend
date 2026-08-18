package com.inventory.officeInventory.orders.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDTO {

    private UUID id;
    private String username;
    private String role;
}