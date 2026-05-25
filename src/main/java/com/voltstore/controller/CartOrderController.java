package com.voltstore.controller;

import com.voltstore.model.*;
import com.voltstore.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CartOrderController {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    public CartOrderController(UserRepository userRepository,
                               CartItemRepository cartItemRepository,
                               OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(auth.getName());
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getCart() {
        Optional<User> userOpt = getAuthenticatedUser();
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<CartItem> dbItems = cartItemRepository.findByUser(userOpt.get());
        List<Map<String, Object>> response = dbItems.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", item.getProductId());
            map.put("quantity", item.getQuantity());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart")
    @Transactional
    public ResponseEntity<?> syncCart(@RequestBody List<Map<String, Object>> cartItems) {
        Optional<User> userOpt = getAuthenticatedUser();
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User user = userOpt.get();
        cartItemRepository.deleteByUser(user);

        for (Map<String, Object> itemMap : cartItems) {
            Integer productId = (Integer) itemMap.get("productId");
            Integer quantity = (Integer) itemMap.get("quantity");
            if (productId != null && quantity != null) {
                cartItemRepository.save(new CartItem(user, productId, quantity));
            }
        }
        return ResponseEntity.ok(Map.of("message", "Cart synchronized successfully"));
    }

    @PostMapping("/orders/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> payload) {
        Optional<User> userOpt = getAuthenticatedUser();
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        User user = userOpt.get();
        
        Double totalAmount = Double.valueOf(payload.get("totalAmount").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        // Extract shipping and payment fields from request payload
        String shippingAddress = (String) payload.get("shippingAddress");
        String city = (String) payload.get("city");
        String zipCode = (String) payload.get("zipCode");
        String phone = (String) payload.get("phone");
        String paymentMethod = (String) payload.get("paymentMethod");

        Order order = new Order(user, LocalDateTime.now(), totalAmount, "Processing",
                shippingAddress, city, zipCode, phone, paymentMethod);
                
        for (Map<String, Object> itemMap : items) {
            Integer productId = (Integer) itemMap.get("productId");
            Integer quantity = (Integer) itemMap.get("quantity");
            Double price = Double.valueOf(itemMap.get("price").toString());
            
            if (productId != null && quantity != null && price != null) {
                OrderItem orderItem = new OrderItem(productId, quantity, price);
                order.addOrderItem(orderItem);
            }
        }

        orderRepository.save(order);
        cartItemRepository.deleteByUser(user); // clear cart in database

        return ResponseEntity.ok(Map.of(
            "message", "Order placed successfully",
            "orderId", order.getId()
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders() {
        Optional<User> userOpt = getAuthenticatedUser();
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(userOpt.get());
        
        List<Map<String, Object>> response = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getId());
            map.put("orderDate", order.getOrderDate().toString());
            map.put("totalAmount", order.getTotalAmount());
            map.put("status", order.getStatus());
            
            // Add shipping/payment fields to the order item JSON
            map.put("shippingAddress", order.getShippingAddress() != null ? order.getShippingAddress() : "Not Provided");
            map.put("city", order.getCity() != null ? order.getCity() : "");
            map.put("zipCode", order.getZipCode() != null ? order.getZipCode() : "");
            map.put("phone", order.getPhone() != null ? order.getPhone() : "Not Provided");
            map.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "Not Provided");
            
            map.put("items", order.getOrderItems().stream().map(item -> {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                return itemMap;
            }).collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
