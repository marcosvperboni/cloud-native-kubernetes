package com.marcosperboni.cloudnative.inventory.application;

import com.marcosperboni.cloudnative.inventory.application.dto.InventoryResponse;
import com.marcosperboni.cloudnative.inventory.domain.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

	public InventoryResponse toResponse(InventoryItem item) {
		return new InventoryResponse(
				item.getId(),
				item.getProductId(),
				item.getQuantityAvailable(),
				item.getQuantityReserved(),
				item.getWarehouseLocation(),
				item.getUpdatedAt());
	}
}
