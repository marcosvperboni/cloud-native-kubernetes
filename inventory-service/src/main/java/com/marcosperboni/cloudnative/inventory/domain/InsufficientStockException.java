package com.marcosperboni.cloudnative.inventory.domain;

public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(String message) {
		super(message);
	}
}
