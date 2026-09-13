package com.marcosperboni.cloudnative.order.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marcosperboni.cloudnative.order.domain.Order;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

	List<Order> findAllByOrderByCreatedAtDesc();

}
