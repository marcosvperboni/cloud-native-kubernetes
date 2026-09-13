package com.marcosperboni.cloudnative.inventory.infrastructure;

public class ProductServiceUnavailableException extends RuntimeException {

	public ProductServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProductServiceUnavailableException(String message) {
		super(message);
	}
}
