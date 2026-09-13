package com.marcosperboni.cloudnative.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "quantity_available", nullable = false)
	private Integer quantityAvailable;

	@Column(name = "quantity_reserved", nullable = false)
	private Integer quantityReserved;

	@Column(name = "warehouse_location", nullable = false, length = 150)
	private String warehouseLocation;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected InventoryItem() {
		// JPA
	}

	public InventoryItem(UUID productId, Integer quantityAvailable, Integer quantityReserved,
			String warehouseLocation, Instant updatedAt) {
		this.productId = productId;
		this.quantityAvailable = quantityAvailable;
		this.quantityReserved = quantityReserved;
		this.warehouseLocation = warehouseLocation;
		this.updatedAt = updatedAt;
	}

	public void reserve(int quantity) {
		if (quantityAvailable < quantity) {
			throw new InsufficientStockException(
					"Insufficient stock for inventory item " + id + ": requested " + quantity
							+ ", available " + quantityAvailable);
		}
		this.quantityAvailable -= quantity;
		this.quantityReserved += quantity;
		this.updatedAt = Instant.now();
	}

	public void update(Integer quantityAvailable, String warehouseLocation) {
		this.quantityAvailable = quantityAvailable;
		this.warehouseLocation = warehouseLocation;
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getProductId() {
		return productId;
	}

	public Integer getQuantityAvailable() {
		return quantityAvailable;
	}

	public Integer getQuantityReserved() {
		return quantityReserved;
	}

	public String getWarehouseLocation() {
		return warehouseLocation;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
