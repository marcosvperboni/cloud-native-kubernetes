package com.marcosperboni.cloudnative.product.domain;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public static ResourceNotFoundException forProduct(UUID id) {
		return new ResourceNotFoundException("Product not found with id: " + id);
	}

}
