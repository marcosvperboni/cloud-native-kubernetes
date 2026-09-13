package com.marcosperboni.cloudnative.inventory.application.dto;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
		UUID id,
		UUID productId,
		Integer quantityAvailable,
		Integer quantityReserved,
		String warehouseLocation,
		Instant updatedAt) {
}
