package com.marcosperboni.cloudnative.inventory.infrastructure;

import java.util.UUID;

public interface ProductClient {

	boolean productExists(UUID productId);
}
