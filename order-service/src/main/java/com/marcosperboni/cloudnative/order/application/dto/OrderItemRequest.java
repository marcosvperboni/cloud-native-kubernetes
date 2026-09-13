package com.marcosperboni.cloudnative.order.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(

		@NotNull UUID productId,

		@NotNull @Positive Integer quantity

) {
}
