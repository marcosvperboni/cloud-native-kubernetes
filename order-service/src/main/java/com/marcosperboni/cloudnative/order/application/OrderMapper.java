package com.marcosperboni.cloudnative.order.application;

import org.springframework.stereotype.Component;

import com.marcosperboni.cloudnative.order.application.dto.OrderItemResponse;
import com.marcosperboni.cloudnative.order.application.dto.OrderResponse;
import com.marcosperboni.cloudnative.order.domain.Order;
import com.marcosperboni.cloudnative.order.domain.OrderItem;

@Component
public class OrderMapper {

	public OrderResponse toResponse(Order order) {
		return new OrderResponse(
				order.getId(),
				order.getCustomerName(),
				order.getStatus(),
				order.getTotalAmount(),
				order.getItems().stream().map(this::toItemResponse).toList(),
				order.getCreatedAt(),
				order.getUpdatedAt());
	}

	public OrderItemResponse toItemResponse(OrderItem item) {
		return new OrderItemResponse(
				item.getProductId(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getLineTotal());
	}

}
