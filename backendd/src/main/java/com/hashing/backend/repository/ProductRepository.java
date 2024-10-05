package com.hashing.backend.repository;

import com.hashing.backend.model.Product;
import com.hashing.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySender(User sender);

    List<Product> findByReceiver(User receiver);

    void deleteAllBySender(User sender);
    void deleteAllByReceiver(User receiver);
}