package com.marcosperboni.cloudnative.order.application.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record OrderRequest(

		@NotBlank @Size(max = 150) String customerName,

		@NotEmpty @Valid List<OrderItemRequest> items

) {
}
