package com.groceryshop.controller;

import com.groceryshop.dto.ApiResponse;
import com.groceryshop.service.FirestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private FirestoreService firestoreService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllCategories() {
        try {
            List<Map<String, Object>> categories = firestoreService.findAll("categories");
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCategoryById(@PathVariable String id) {
        try {
            Map<String, Object> category = firestoreService.findById("categories", id);
            if (category == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve category: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(@RequestBody Map<String, Object> categoryData) {
        try {
            categoryData.put("isActive", true);
            categoryData.put("createdAt", LocalDateTime.now().toString());
            categoryData.put("updatedAt", LocalDateTime.now().toString());

            String categoryId = firestoreService.save("categories", categoryData);
            categoryData.put("id", categoryId);

            return ResponseEntity.ok(ApiResponse.success("Category created successfully", categoryData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create category: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCategory(
            @PathVariable String id, @RequestBody Map<String, Object> updates) {
        try {
            Map<String, Object> existingCategory = firestoreService.findById("categories", id);
            if (existingCategory == null) {
                return ResponseEntity.notFound().build();
            }

            updates.put("updatedAt", LocalDateTime.now().toString());
            firestoreService.update("categories", id, updates);

            Map<String, Object> updatedCategory = firestoreService.findById("categories", id);
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updatedCategory));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update category: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable String id) {
        try {
            boolean deleted = firestoreService.delete("categories", id);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete category: " + e.getMessage()));
        }
    }
}
