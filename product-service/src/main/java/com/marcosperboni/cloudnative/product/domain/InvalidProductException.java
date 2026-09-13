package com.marcosperboni.cloudnative.product.domain;

public class InvalidProductException extends RuntimeException {

	public InvalidProductException(String message) {
		super(message);
	}

}
