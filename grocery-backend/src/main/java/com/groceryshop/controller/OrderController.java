package com.groceryshop.controller;

import com.groceryshop.dto.ApiResponse;
import com.groceryshop.service.EmailService;
import com.groceryshop.service.FirestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private FirestoreService firestoreService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status) {
        try {
            List<Map<String, Object>> orders = firestoreService.findAll("orders");
            
            // Filter by customer if provided
            if (customerId != null && !customerId.isEmpty()) {
                orders = orders.stream()
                        .filter(order -> customerId.equals(order.get("customerId")))
                        .collect(Collectors.toList());
            }
            
            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                orders = orders.stream()
                        .filter(order -> status.equals(order.get("status")))
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderById(@PathVariable String id) {
        try {
            Map<String, Object> order = firestoreService.findById("orders", id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            orderData.put("status", "PENDING");
            orderData.put("orderDate", LocalDateTime.now().toString());

            String orderId = firestoreService.save("orders", orderData);
            orderData.put("id", orderId);

            // Send notification to admin
            List<Map<String, Object>> admins = firestoreService.findByField("users", "role", "ADMIN");
            if (!admins.isEmpty()) {
                String adminEmail = (String) admins.get(0).get("email");
                String orderDetails = formatOrderDetails(orderData);
                emailService.sendOrderNotification(adminEmail, orderDetails);
            }

            return ResponseEntity.ok(ApiResponse.success("Order created successfully", orderData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOrderStatus(
            @PathVariable String id, @RequestBody Map<String, Object> statusUpdate) {
        try {
            Map<String, Object> order = firestoreService.findById("orders", id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            String newStatus = (String) statusUpdate.get("status");
            String deliveryTime = (String) statusUpdate.get("deliveryTime");
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus);
            updates.put("updatedAt", LocalDateTime.now().toString());
            
            if (deliveryTime != null) {
                updates.put("deliveryTime", deliveryTime);
            }

            firestoreService.update("orders", id, updates);
            Map<String, Object> updatedOrder = firestoreService.findById("orders", id);

            // Send confirmation email to customer if order is confirmed
            if ("CONFIRMED".equals(newStatus)) {
                String customerEmail = (String) order.get("customerEmail");
                String orderDetails = formatOrderDetails(updatedOrder);
                String estimatedDelivery = deliveryTime != null ? deliveryTime : "2-3 hours";
                emailService.sendOrderConfirmation(customerEmail, orderDetails, estimatedDelivery);
            }

            return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCustomerOrders(@PathVariable String customerId) {
        try {
            List<Map<String, Object>> orders = firestoreService.findByField("orders", "customerId", customerId);
            return ResponseEntity.ok(ApiResponse.success("Customer orders retrieved successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve customer orders: " + e.getMessage()));
        }
    }

    private String formatOrderDetails(Map<String, Object> order) {
        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(order.get("id")).append("\n");
        details.append("Customer Email: ").append(order.get("customerEmail")).append("\n");
        details.append("Total Amount: ₹").append(order.get("totalAmount")).append("\n");
        details.append("Delivery Address: ").append(order.get("deliveryAddress")).append("\n");
        details.append("Phone: ").append(order.get("customerPhone")).append("\n");
        details.append("Order Date: ").append(order.get("orderDate")).append("\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        if (items != null) {
            details.append("\nItems:\n");
            for (Map<String, Object> item : items) {
                details.append("- ").append(item.get("productName"))
                       .append(" x ").append(item.get("quantity"))
                       .append(" = ₹").append(item.get("totalPrice")).append("\n");
            }
        }
        
        return details.toString();
    }
}
