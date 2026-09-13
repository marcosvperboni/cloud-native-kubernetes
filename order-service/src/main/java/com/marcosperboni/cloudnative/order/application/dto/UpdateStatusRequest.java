package com.marcosperboni.cloudnative.order.application.dto;

import com.marcosperboni.cloudnative.order.domain.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

		@NotNull OrderStatus status

) {
}
