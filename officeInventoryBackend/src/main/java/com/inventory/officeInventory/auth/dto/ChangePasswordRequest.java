package com.inventory.officeInventory.auth.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String currentPassword;

    private String newPassword;
}