package com.marcosperboni.cloudnative.order.interfaces.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.marcosperboni.cloudnative.order.application.OrderService;
import com.marcosperboni.cloudnative.order.application.dto.OrderRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderResponse;
import com.marcosperboni.cloudnative.order.application.dto.UpdateStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> findAll() {
		return ResponseEntity.ok(orderService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(orderService.findById(id));
	}

	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request, UriComponentsBuilder uriBuilder) {
		OrderResponse response = orderService.createOrder(request);
		URI location = uriBuilder.path("/api/orders/{id}").buildAndExpand(response.id()).toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
		return ResponseEntity.ok(orderService.updateStatus(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		orderService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
