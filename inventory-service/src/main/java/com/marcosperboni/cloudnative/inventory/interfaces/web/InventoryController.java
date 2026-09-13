package com.marcosperboni.cloudnative.inventory.interfaces.web;

import com.marcosperboni.cloudnative.inventory.application.InventoryService;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryResponse;
import com.marcosperboni.cloudnative.inventory.application.dto.ReserveByProductRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.ReserveStockRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@GetMapping
	public List<InventoryResponse> findAll() {
		return inventoryService.findAll();
	}

	@GetMapping("/{id}")
	public InventoryResponse findById(@PathVariable UUID id) {
		return inventoryService.findById(id);
	}

	@PostMapping
	public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryRequest request,
			UriComponentsBuilder uriBuilder) {
		InventoryResponse response = inventoryService.create(request);
		URI location = uriBuilder.path("/api/inventory/{id}").buildAndExpand(response.id()).toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PutMapping("/{id}")
	public InventoryResponse update(@PathVariable UUID id, @Valid @RequestBody InventoryRequest request) {
		return inventoryService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		inventoryService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/reserve")
	public InventoryResponse reserve(@PathVariable UUID id, @Valid @RequestBody ReserveStockRequest request) {
		return inventoryService.reserveStock(id, request.quantity());
	}

	@PostMapping("/reserve-by-product")
	public InventoryResponse reserveByProduct(@Valid @RequestBody ReserveByProductRequest request) {
		return inventoryService.reserveStockByProduct(request.productId(), request.quantity());
	}
}
