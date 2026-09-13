package com.marcosperboni.cloudnative.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(

		UUID productId,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal lineTotal

) {
}
