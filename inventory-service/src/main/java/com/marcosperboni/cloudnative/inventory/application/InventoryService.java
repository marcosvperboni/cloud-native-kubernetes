package com.marcosperboni.cloudnative.inventory.application;

import com.marcosperboni.cloudnative.inventory.application.dto.InventoryRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

	InventoryResponse create(InventoryRequest request);

	InventoryResponse findById(UUID id);

	List<InventoryResponse> findAll();

	InventoryResponse update(UUID id, InventoryRequest request);

	void delete(UUID id);

	InventoryResponse reserveStock(UUID id, int quantity);

	InventoryResponse reserveStockByProduct(UUID productId, int quantity);
}
