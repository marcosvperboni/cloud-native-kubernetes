package com.marcosperboni.cloudnative.order.domain;

import java.util.Set;

public enum OrderStatus {

	PENDING,
	CONFIRMED,
	CANCELLED;

	public boolean canTransitionTo(OrderStatus target) {
		return switch (this) {
			case PENDING -> Set.of(CONFIRMED, CANCELLED).contains(target);
			case CONFIRMED -> target == CANCELLED;
			case CANCELLED -> false;
		};
	}

}
