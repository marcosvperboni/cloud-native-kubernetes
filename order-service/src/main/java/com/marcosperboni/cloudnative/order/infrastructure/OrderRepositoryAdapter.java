package com.marcosperboni.cloudnative.order.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.marcosperboni.cloudnative.order.domain.Order;
import com.marcosperboni.cloudnative.order.domain.OrderRepository;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

	private final OrderJpaRepository jpaRepository;

	public OrderRepositoryAdapter(OrderJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Order save(Order order) {
		return jpaRepository.save(order);
	}

	@Override
	public Optional<Order> findById(UUID id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<Order> findAllByOrderByCreatedAtDesc() {
		return jpaRepository.findAllByOrderByCreatedAtDesc();
	}

	@Override
	public void deleteById(UUID id) {
		jpaRepository.deleteById(id);
	}

}
