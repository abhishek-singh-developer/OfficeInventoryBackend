package com.inventory.officeInventory.orders.mapper;

import com.inventory.officeInventory.orders.dto.OrderItemResponseDTO;
import com.inventory.officeInventory.orders.dto.OrderResponseDTO;
import com.inventory.officeInventory.orders.dto.UserResponseDTO;
import com.inventory.officeInventory.orders.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public static OrderResponseDTO mapToDTO(Order order) {

        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(order.getStatus());
        dto.setExpiryDate(order.getExpiryDate());
        dto.setTransactionReference(order.getTransactionReference());
        dto.setRejectionNote(order.getRejectionNote());

        // Created By
        if (order.getCreatedBy() != null) {

            UserResponseDTO userDTO = new UserResponseDTO();

            userDTO.setId(order.getCreatedBy().getId());
            userDTO.setUsername(order.getCreatedBy().getUsername());
            userDTO.setRole(order.getCreatedBy().getRole().name());

            dto.setCreatedBy(userDTO);
        }

        // Items
        if (order.getItems() != null) {

            dto.setItems(
                    order.getItems()
                            .stream()
                            .map(item -> {

                                OrderItemResponseDTO itemDTO =
                                        new OrderItemResponseDTO();

                                itemDTO.setId(item.getId());
                                itemDTO.setItemName(item.getItemName());
                                itemDTO.setQuantity(item.getQuantity());

                                return itemDTO;
                            })
                            .toList()
            );
        }

        return dto;
    }
}