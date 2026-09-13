package com.marcosperboni.cloudnative.order.domain;

public class DownstreamServiceUnavailableException extends RuntimeException {

	public DownstreamServiceUnavailableException(String message) {
		super(message);
	}

}
