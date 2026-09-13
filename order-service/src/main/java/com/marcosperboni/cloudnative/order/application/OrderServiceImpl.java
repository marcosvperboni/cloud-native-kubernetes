package com.marcosperboni.cloudnative.order.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcosperboni.cloudnative.order.application.dto.OrderRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderResponse;
import com.marcosperboni.cloudnative.order.application.dto.UpdateStatusRequest;
import com.marcosperboni.cloudnative.order.domain.InvalidOrderException;
import com.marcosperboni.cloudnative.order.domain.Order;
import com.marcosperboni.cloudnative.order.domain.OrderItem;
import com.marcosperboni.cloudnative.order.domain.OrderRepository;
import com.marcosperboni.cloudnative.order.domain.OrderStatus;
import com.marcosperboni.cloudnative.order.domain.ResourceNotFoundException;
import com.marcosperboni.cloudnative.order.infrastructure.InventoryClient;
import com.marcosperboni.cloudnative.order.infrastructure.ProductClient;
import com.marcosperboni.cloudnative.order.infrastructure.ProductClient.ProductInfo;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final ProductClient productClient;
	private final InventoryClient inventoryClient;
	private final OrderMapper orderMapper;

	public OrderServiceImpl(
			OrderRepository orderRepository,
			ProductClient productClient,
			InventoryClient inventoryClient,
			OrderMapper orderMapper) {
		this.orderRepository = orderRepository;
		this.productClient = productClient;
		this.inventoryClient = inventoryClient;
		this.orderMapper = orderMapper;
	}

	@Override
	public OrderResponse createOrder(OrderRequest request) {
		Order order = new Order(request.customerName());

		request.items().forEach(itemRequest -> {
			ProductInfo product = productClient.getProduct(itemRequest.productId());
			inventoryClient.reserveStock(itemRequest.productId(), itemRequest.quantity());
			order.addItem(new OrderItem(itemRequest.productId(), itemRequest.quantity(), product.price()));
		});

		order.recalculateTotal();

		return orderMapper.toResponse(orderRepository.save(order));
	}

	@Override
	public OrderResponse updateStatus(UUID id, UpdateStatusRequest request) {
		Order order = getOrderOrThrow(id);
		order.changeStatus(request.status());
		return orderMapper.toResponse(orderRepository.save(order));
	}

	@Override
	@Transactional(readOnly = true)
	public OrderResponse findById(UUID id) {
		return orderMapper.toResponse(getOrderOrThrow(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponse> findAll() {
		return orderRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(orderMapper::toResponse)
				.toList();
	}

	@Override
	public void delete(UUID id) {
		Order order = getOrderOrThrow(id);
		if (order.getStatus() == OrderStatus.CONFIRMED) {
			throw InvalidOrderException.deleteConfirmed();
		}
		orderRepository.deleteById(id);
	}

	private Order getOrderOrThrow(UUID id) {
		return orderRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forOrder(id));
	}

}
