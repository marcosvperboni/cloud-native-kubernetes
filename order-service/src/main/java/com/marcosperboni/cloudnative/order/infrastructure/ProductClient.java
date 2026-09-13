package com.marcosperboni.cloudnative.order.infrastructure;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductClient {

	ProductInfo getProduct(UUID productId);

	record ProductInfo(UUID id, String name, BigDecimal price) {
	}

}
