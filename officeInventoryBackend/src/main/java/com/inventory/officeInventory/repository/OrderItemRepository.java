package com.inventory.officeInventory.repository;

import com.inventory.officeInventory.enums.OrderStatus;
import com.inventory.officeInventory.orders.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
        SELECT COUNT(oi) > 0
        FROM OrderItem oi
        WHERE LOWER(oi.itemName) = LOWER(:itemName)
        AND oi.order.status = :status
        AND oi.order.id <> :orderId
    """)
    boolean existsInSubmittedOrder(
            @Param("itemName") String itemName,
            @Param("status") OrderStatus status,
            @Param("orderId") UUID orderId
    );
}