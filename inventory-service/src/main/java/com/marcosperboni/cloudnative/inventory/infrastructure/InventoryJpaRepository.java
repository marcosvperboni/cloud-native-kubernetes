package com.marcosperboni.cloudnative.inventory.infrastructure;

import com.marcosperboni.cloudnative.inventory.domain.InventoryItem;
import com.marcosperboni.cloudnative.inventory.domain.InventoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryJpaRepository extends JpaRepository<InventoryItem, UUID>, InventoryRepository {
}
