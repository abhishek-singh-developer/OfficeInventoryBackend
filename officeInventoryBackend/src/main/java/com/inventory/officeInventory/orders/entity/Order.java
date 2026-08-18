package com.inventory.officeInventory.orders.entity;

import com.inventory.officeInventory.enums.OrderStatus;
import com.inventory.officeInventory.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String orderNo;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String transactionReference;

    private String rejectionNote;

    @ManyToOne
    private User createdBy;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();


}