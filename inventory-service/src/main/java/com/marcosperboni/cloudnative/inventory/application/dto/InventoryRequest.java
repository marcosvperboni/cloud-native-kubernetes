package com.marcosperboni.cloudnative.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record InventoryRequest(
		@NotNull UUID productId,
		@NotNull @PositiveOrZero Integer quantityAvailable,
		@NotBlank String warehouseLocation) {
}
