package com.groceryshop.controller;

import com.groceryshop.dto.ApiResponse;
import com.groceryshop.service.FirestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private FirestoreService firestoreService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search) {
        try {
            List<Map<String, Object>> products = firestoreService.findAll("products");
            
            // Filter by category if provided
            if (categoryId != null && !categoryId.isEmpty()) {
                products = products.stream()
                        .filter(product -> categoryId.equals(product.get("categoryId")))
                        .collect(Collectors.toList());
            }
            
            // Filter by search term if provided
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                products = products.stream()
                        .filter(product -> {
                            String name = (String) product.get("name");
                            String description = (String) product.get("description");
                            return (name != null && name.toLowerCase().contains(searchLower)) ||
                                   (description != null && description.toLowerCase().contains(searchLower));
                        })
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve products: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductById(@PathVariable String id) {
        try {
            Map<String, Object> product = firestoreService.findById("products", id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve product: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProduct(@RequestBody Map<String, Object> productData) {
        try {
            productData.put("isActive", true);
            productData.put("createdAt", LocalDateTime.now().toString());
            productData.put("updatedAt", LocalDateTime.now().toString());

            String productId = firestoreService.save("products", productData);
            productData.put("id", productId);

            return ResponseEntity.ok(ApiResponse.success("Product created successfully", productData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProduct(
            @PathVariable String id, @RequestBody Map<String, Object> updates) {
        try {
            Map<String, Object> existingProduct = firestoreService.findById("products", id);
            if (existingProduct == null) {
                return ResponseEntity.notFound().build();
            }

            updates.put("updatedAt", LocalDateTime.now().toString());
            firestoreService.update("products", id, updates);

            Map<String, Object> updatedProduct = firestoreService.findById("products", id);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String id) {
        try {
            boolean deleted = firestoreService.delete("products", id);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStock(
            @PathVariable String id, @RequestBody Map<String, Object> stockUpdate) {
        try {
            Map<String, Object> product = firestoreService.findById("products", id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            Integer newStock = (Integer) stockUpdate.get("stockQuantity");
            if (newStock == null || newStock < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid stock quantity"));
            }

            Map<String, Object> updates = Map.of(
                "stockQuantity", newStock,
                "updatedAt", LocalDateTime.now().toString()
            );
            
            firestoreService.update("products", id, updates);
            Map<String, Object> updatedProduct = firestoreService.findById("products", id);

            return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update stock: " + e.getMessage()));
        }
    }
}
