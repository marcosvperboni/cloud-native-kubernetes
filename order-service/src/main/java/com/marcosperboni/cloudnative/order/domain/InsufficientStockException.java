package com.marcosperboni.cloudnative.order.domain;

/**
 * Raised when inventory-service reports that a product cannot cover the requested quantity.
 * Extends {@link InvalidOrderException} so it maps to the same 409 response without extra
 * handler wiring.
 */
public class InsufficientStockException extends InvalidOrderException {

	public InsufficientStockException(String message) {
		super(message);
	}

}
