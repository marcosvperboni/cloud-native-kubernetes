package com.marcosperboni.cloudnative.product.application;

import com.marcosperboni.cloudnative.product.application.dto.ProductResponse;
import com.marcosperboni.cloudnative.product.domain.Product;

public final class ProductMapper {

	private ProductMapper() {
	}

	public static ProductResponse toResponse(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				product.isActive(),
				product.getCreatedAt(),
				product.getUpdatedAt());
	}

}
