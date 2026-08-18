package com.inventory.officeInventory.orders;

import com.inventory.officeInventory.exception.UnauthorizedActionException;
import com.inventory.officeInventory.orders.dto.CompleteOrderRequestDTO;
import com.inventory.officeInventory.orders.dto.OrderRequest;
import com.inventory.officeInventory.orders.dto.OrderResponseDTO;
import com.inventory.officeInventory.orders.dto.RejectOrderRequestDTO;
import com.inventory.officeInventory.orders.entity.Order;
import com.inventory.officeInventory.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOrders() {

        return ResponseEntity.ok(
                orderService.getOrders()
        );
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestBody OrderRequest request) {

        return ResponseEntity.ok(
                orderService.createOrder(request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                orderService.getOrderById(id)
        );
    }

    @PatchMapping("/{id}/submit")
    public OrderResponseDTO submitOrder(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return orderService.submitOrder(id, authentication);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable UUID id,
            @RequestBody OrderRequest request,
            Authentication authentication
    ) {

        OrderResponseDTO response =
                orderService.updateOrder(
                        id,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderResponseDTO> completeOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CompleteOrderRequestDTO request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                orderService.completeOrder(
                        orderId,
                        request,
                        authentication
                )
        );
    }

    @PostMapping("/{orderId}/reject")
    public ResponseEntity<OrderResponseDTO> rejectOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody RejectOrderRequestDTO request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                orderService.rejectOrder(
                        orderId,
                        request,
                        authentication
                )
        );
    }
}