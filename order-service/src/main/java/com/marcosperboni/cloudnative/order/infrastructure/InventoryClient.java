package com.marcosperboni.cloudnative.order.infrastructure;

import java.util.UUID;

public interface InventoryClient {

	void reserveStock(UUID productId, int quantity);

}
