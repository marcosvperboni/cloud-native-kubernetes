package com.marcosperboni.cloudnative.product.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(

		@NotBlank @Size(max = 150) String name,

		String description,

		@NotNull @DecimalMin(value = "0.01") BigDecimal price,

		@NotNull @PositiveOrZero Integer stockQuantity

) {
}
