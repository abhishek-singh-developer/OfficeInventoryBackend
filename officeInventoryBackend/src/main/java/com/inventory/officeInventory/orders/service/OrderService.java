package com.inventory.officeInventory.orders.service;

import com.inventory.officeInventory.enums.OrderStatus;
import com.inventory.officeInventory.exception.BusinessValidation;
import com.inventory.officeInventory.exception.UnauthorizedActionException;
import com.inventory.officeInventory.orders.dto.CompleteOrderRequestDTO;
import com.inventory.officeInventory.orders.dto.OrderRequest;
import com.inventory.officeInventory.orders.dto.OrderResponseDTO;
import com.inventory.officeInventory.orders.dto.RejectOrderRequestDTO;
import com.inventory.officeInventory.orders.entity.Order;
import com.inventory.officeInventory.orders.entity.OrderItem;
import com.inventory.officeInventory.orders.mapper.OrderMapper;
import com.inventory.officeInventory.repository.OrderItemRepository;
import com.inventory.officeInventory.repository.OrderRepository;
import com.inventory.officeInventory.repository.UserRepository;
import com.inventory.officeInventory.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public List<OrderResponseDTO> getOrders() {

        /*String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow();*/

        List<Order> orders =
                orderRepository.findAll();

        return orders.stream()
                .map(OrderMapper::mapToDTO)
                .toList();
    }


    public OrderResponseDTO createOrder(OrderRequest request) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );

        Order order = new Order();

        order.setOrderNo(
                "ORD-" + System.currentTimeMillis()
        );

        order.setExpiryDate(request.getExpiryDate());

        order.setStatus(OrderStatus.DRAFT);

        order.setCreatedBy(user);

        List<OrderItem> items = request.getItems()
                .stream()
                .map(itemRequest -> {

                    OrderItem item = new OrderItem();

                    item.setItemName(
                            itemRequest.getItemName()
                    );

                    item.setQuantity(
                            itemRequest.getQuantity()
                    );

                    item.setOrder(order);

                    return item;

                })
                .toList();

        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.mapToDTO(savedOrder);
    }
    public OrderResponseDTO submitOrder(
            UUID orderId,
            Authentication authentication
    ) {

        authentication.getAuthorities()
                .stream()
                .forEach(f ->
                {
                 log.info("Roles : {}", f.getAuthority());
                });

        boolean isManager = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_MANAGER")
                );
        log.info("isManager : {}", isManager);
        if (!isManager) {
            throw new UnauthorizedActionException(
                    "You are not authorized to submit this order"
            );
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        // Get currently logged-in user
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        // Only draft can be submitted
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new RuntimeException(
                    "Only draft orders can be submitted"
            );
        }

        // Validate duplicate items
        for (OrderItem item : order.getItems()) {

            boolean alreadyExists =
                    orderItemRepository.existsInSubmittedOrder(
                            item.getItemName(),
                            OrderStatus.SUBMITTED,
                            order.getId()
                    );

            if (alreadyExists) {
                throw new BusinessValidation(
                        "Item already exists in another submitted order: "
                                + item.getItemName()
                );
            }
        }

        // Submit order
        order.setStatus(OrderStatus.SUBMITTED);

        Order savedOrder = orderRepository.save(order);

        return OrderMapper.mapToDTO(savedOrder);
    }

    @Transactional
    public OrderResponseDTO updateOrder(
            UUID orderId,
            OrderRequest request,
            String username
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new BusinessValidation("Order not found")
                );

        // Check owner
        if (!order.getCreatedBy().getUsername().equals(username)) {
            throw new BusinessValidation(
                    "You can only edit your own order"
            );
        }

        // Check status
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessValidation(
                    "Only draft orders can be edited"
            );
        }

        order.setExpiryDate(request.getExpiryDate());

        // Remove existing items
        order.getItems().clear();

        // Add updated items
        if (request.getItems() != null) {

            request.getItems().forEach(itemRequest -> {

                OrderItem item = new OrderItem();

                item.setItemName(itemRequest.getItemName());
                item.setQuantity(itemRequest.getQuantity());
                item.setOrder(order);

                order.getItems().add(item);
            });
        }

        Order savedOrder = orderRepository.save(order);

        return orderMapper.mapToDTO(savedOrder);
    }

    public OrderResponseDTO getOrderById(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

        return orderMapper.mapToDTO(order);
    }

    @Transactional
    public OrderResponseDTO completeOrder(
            UUID orderId,
            CompleteOrderRequestDTO request,
            Authentication authentication
    ) {

        log.info(
                "Completing order: {} by user: {}",
                orderId,
                authentication.getName()
        );

        boolean isPurchaser = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_PURCHASER")
                );

        if (!isPurchaser) {

            throw new UnauthorizedActionException(
                    "You are not authorized to complete this order"
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );

        if (order.getStatus() != OrderStatus.SUBMITTED) {

            throw new UnauthorizedActionException(
                    "Only submitted orders can be completed"
            );
        }

        if (request.getTransactionReference() == null ||
                request.getTransactionReference().isBlank()) {

            throw new RuntimeException(
                    "Transaction reference is required"
            );
        }

        order.setTransactionReference(
                request.getTransactionReference().trim()
        );

        order.setStatus(OrderStatus.COMPLETED);

        Order savedOrder =
                orderRepository.save(order);

        log.info(
                "Order {} completed successfully with transaction reference {}",
                orderId,
                request.getTransactionReference()
        );

        return OrderMapper.mapToDTO(savedOrder);
    }

    @Transactional
    public OrderResponseDTO rejectOrder(
            UUID orderId,
            RejectOrderRequestDTO request,
            Authentication authentication
    ) {

        log.info(
                "Rejecting order: {} by user: {}",
                orderId,
                authentication.getName()
        );

        boolean isPurchaser = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_PURCHASER")
                );

        if (!isPurchaser) {

            throw new UnauthorizedActionException(
                    "You are not authorized to reject this order"
            );
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );

        if (order.getStatus() != OrderStatus.SUBMITTED) {

            throw new UnauthorizedActionException(
                    "Only submitted orders can be rejected"
            );
        }

        if (request.getRejectionNote() == null ||
                request.getRejectionNote().isBlank()) {

            throw new RuntimeException(
                    "Rejection note is required"
            );
        }

        order.setRejectionNote(
                request.getRejectionNote().trim()
        );

        order.setStatus(OrderStatus.REJECTED);

        Order savedOrder =
                orderRepository.save(order);

        log.info(
                "Order {} rejected. Reason: {}",
                orderId,
                request.getRejectionNote()
        );

        return OrderMapper.mapToDTO(savedOrder);
    }
}
