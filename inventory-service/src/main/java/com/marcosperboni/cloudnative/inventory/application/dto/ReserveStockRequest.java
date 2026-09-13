package com.marcosperboni.cloudnative.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveStockRequest(@NotNull @Positive Integer quantity) {
}
