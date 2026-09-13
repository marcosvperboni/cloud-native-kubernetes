package com.marcosperboni.cloudnative.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.marcosperboni.cloudnative.order.domain.OrderStatus;

public record OrderResponse(

		UUID id,
		String customerName,
		OrderStatus status,
		BigDecimal totalAmount,
		List<OrderItemResponse> items,
		Instant createdAt,
		Instant updatedAt

) {
}
