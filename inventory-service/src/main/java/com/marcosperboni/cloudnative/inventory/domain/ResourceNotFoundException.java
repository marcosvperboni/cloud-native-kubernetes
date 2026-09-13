package com.marcosperboni.cloudnative.inventory.domain;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
