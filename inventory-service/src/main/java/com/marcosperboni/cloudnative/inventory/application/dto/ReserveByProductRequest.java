package com.marcosperboni.cloudnative.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReserveByProductRequest(@NotNull UUID productId, @NotNull @Positive Integer quantity) {
}
