package com.marcosperboni.cloudnative.order.application;

import java.util.List;
import java.util.UUID;

import com.marcosperboni.cloudnative.order.application.dto.OrderRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderResponse;
import com.marcosperboni.cloudnative.order.application.dto.UpdateStatusRequest;

public interface OrderService {

	OrderResponse createOrder(OrderRequest request);

	OrderResponse updateStatus(UUID id, UpdateStatusRequest request);

	OrderResponse findById(UUID id);

	List<OrderResponse> findAll();

	void delete(UUID id);

}
