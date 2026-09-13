package com.marcosperboni.cloudnative.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

	InventoryItem save(InventoryItem item);

	Optional<InventoryItem> findById(UUID id);

	List<InventoryItem> findAll();

	Optional<InventoryItem> findByProductId(UUID productId);

	void deleteById(UUID id);

	boolean existsById(UUID id);
}
