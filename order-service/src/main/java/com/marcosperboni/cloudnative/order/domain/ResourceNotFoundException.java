package com.marcosperboni.cloudnative.order.domain;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public static ResourceNotFoundException forOrder(UUID id) {
		return new ResourceNotFoundException("Order not found with id: " + id);
	}

}
