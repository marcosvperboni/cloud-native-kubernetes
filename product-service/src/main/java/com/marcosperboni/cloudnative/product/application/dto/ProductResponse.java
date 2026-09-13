package com.marcosperboni.cloudnative.product.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(

		UUID id,
		String name,
		String description,
		BigDecimal price,
		Integer stockQuantity,
		boolean active,
		Instant createdAt,
		Instant updatedAt

) {
}
