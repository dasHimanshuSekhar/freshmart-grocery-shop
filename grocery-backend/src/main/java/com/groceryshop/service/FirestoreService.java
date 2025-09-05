package com.groceryshop.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FirestoreService {
    
    // For demo purposes, using in-memory storage instead of actual Firestore
    // In production, you would use actual Firestore operations
    private final Map<String, Map<String, Object>> mockDatabase = new ConcurrentHashMap<>();
    
    public FirestoreService() {
        // Initialize collections
        mockDatabase.put("users", new ConcurrentHashMap<>());
        mockDatabase.put("categories", new ConcurrentHashMap<>());
        mockDatabase.put("products", new ConcurrentHashMap<>());
        mockDatabase.put("orders", new ConcurrentHashMap<>());
    }

    public String save(String collection, Map<String, Object> data) {
        String id = UUID.randomUUID().toString();
        data.put("id", id);
        mockDatabase.get(collection).put(id, data);
        return id;
    }

    public String save(String collection, String id, Map<String, Object> data) {
        data.put("id", id);
        mockDatabase.get(collection).put(id, data);
        return id;
    }

    public Map<String, Object> findById(String collection, String id) {
        return mockDatabase.get(collection).get(id);
    }

    public List<Map<String, Object>> findAll(String collection) {
        return new ArrayList<>(mockDatabase.get(collection).values());
    }

    public List<Map<String, Object>> findByField(String collection, String field, Object value) {
        return mockDatabase.get(collection).values().stream()
                .filter(doc -> Objects.equals(doc.get(field), value))
                .toList();
    }

    public boolean delete(String collection, String id) {
        return mockDatabase.get(collection).remove(id) != null;
    }

    public void update(String collection, String id, Map<String, Object> updates) {
        Map<String, Object> existing = mockDatabase.get(collection).get(id);
        if (existing != null) {
            existing.putAll(updates);
        }
    }

    // Uncomment below for actual Firestore implementation
    /*
    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public String save(String collection, Map<String, Object> data) {
        try {
            DocumentReference docRef = getFirestore().collection(collection).document();
            data.put("id", docRef.getId());
            docRef.set(data).get();
            return docRef.getId();
        } catch (Exception e) {
            throw new RuntimeException("Error saving document", e);
        }
    }

    public Map<String, Object> findById(String collection, String id) {
        try {
            DocumentSnapshot document = getFirestore().collection(collection).document(id).get().get();
            return document.exists() ? document.getData() : null;
        } catch (Exception e) {
            throw new RuntimeException("Error finding document", e);
        }
    }
    */
}
