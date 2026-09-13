package com.marcosperboni.cloudnative.order.domain;

public class InvalidOrderException extends RuntimeException {

	public InvalidOrderException(String message) {
		super(message);
	}

	public static InvalidOrderException emptyItems() {
		return new InvalidOrderException("An order must contain at least one item");
	}

	public static InvalidOrderException illegalTransition(OrderStatus from, OrderStatus to) {
		return new InvalidOrderException("Cannot transition order from " + from + " to " + to);
	}

	public static InvalidOrderException deleteConfirmed() {
		return new InvalidOrderException("Cannot delete an order with status CONFIRMED; cancel it instead");
	}

}
