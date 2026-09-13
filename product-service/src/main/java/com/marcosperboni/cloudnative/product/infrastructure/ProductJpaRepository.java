package com.marcosperboni.cloudnative.product.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marcosperboni.cloudnative.product.domain.Product;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

	List<Product> findAllByOrderByNameAsc();

}
