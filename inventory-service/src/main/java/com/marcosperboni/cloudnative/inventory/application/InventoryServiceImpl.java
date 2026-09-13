package com.marcosperboni.cloudnative.inventory.application;

import com.marcosperboni.cloudnative.inventory.application.dto.InventoryRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryResponse;
import com.marcosperboni.cloudnative.inventory.domain.InventoryItem;
import com.marcosperboni.cloudnative.inventory.domain.InventoryRepository;
import com.marcosperboni.cloudnative.inventory.domain.ResourceNotFoundException;
import com.marcosperboni.cloudnative.inventory.infrastructure.ProductClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository inventoryRepository;
	private final ProductClient productClient;
	private final InventoryMapper mapper;

	public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductClient productClient,
			InventoryMapper mapper) {
		this.inventoryRepository = inventoryRepository;
		this.productClient = productClient;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public InventoryResponse create(InventoryRequest request) {
		if (!productClient.productExists(request.productId())) {
			throw new ResourceNotFoundException("Product not found: " + request.productId());
		}
		InventoryItem item = new InventoryItem(request.productId(), request.quantityAvailable(), 0,
				request.warehouseLocation(), Instant.now());
		return mapper.toResponse(inventoryRepository.save(item));
	}

	@Override
	public InventoryResponse findById(UUID id) {
		return mapper.toResponse(getOrThrow(id));
	}

	@Override
	public List<InventoryResponse> findAll() {
		return inventoryRepository.findAll().stream().map(mapper::toResponse).toList();
	}

	@Override
	@Transactional
	public InventoryResponse update(UUID id, InventoryRequest request) {
		InventoryItem item = getOrThrow(id);
		item.update(request.quantityAvailable(), request.warehouseLocation());
		return mapper.toResponse(inventoryRepository.save(item));
	}

	@Override
	@Transactional
	public void delete(UUID id) {
		if (!inventoryRepository.existsById(id)) {
			throw new ResourceNotFoundException("Inventory item not found: " + id);
		}
		inventoryRepository.deleteById(id);
	}

	@Override
	@Transactional
	public InventoryResponse reserveStock(UUID id, int quantity) {
		InventoryItem item = getOrThrow(id);
		item.reserve(quantity);
		return mapper.toResponse(inventoryRepository.save(item));
	}

	@Override
	@Transactional
	public InventoryResponse reserveStockByProduct(UUID productId, int quantity) {
		InventoryItem item = inventoryRepository.findByProductId(productId)
				.orElseThrow(() -> new ResourceNotFoundException("No inventory found for product: " + productId));
		item.reserve(quantity);
		return mapper.toResponse(inventoryRepository.save(item));
	}

	private InventoryItem getOrThrow(UUID id) {
		return inventoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
	}
}
