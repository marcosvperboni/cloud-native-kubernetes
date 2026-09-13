package com.marcosperboni.cloudnative.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marcosperboni.cloudnative.order.application.dto.OrderItemRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderRequest;
import com.marcosperboni.cloudnative.order.application.dto.OrderResponse;
import com.marcosperboni.cloudnative.order.application.dto.UpdateStatusRequest;
import com.marcosperboni.cloudnative.order.domain.DownstreamServiceUnavailableException;
import com.marcosperboni.cloudnative.order.domain.InsufficientStockException;
import com.marcosperboni.cloudnative.order.domain.InvalidOrderException;
import com.marcosperboni.cloudnative.order.domain.Order;
import com.marcosperboni.cloudnative.order.domain.OrderRepository;
import com.marcosperboni.cloudnative.order.domain.OrderStatus;
import com.marcosperboni.cloudnative.order.domain.ResourceNotFoundException;
import com.marcosperboni.cloudnative.order.infrastructure.InventoryClient;
import com.marcosperboni.cloudnative.order.infrastructure.ProductClient;
import com.marcosperboni.cloudnative.order.infrastructure.ProductClient.ProductInfo;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ProductClient productClient;

	@Mock
	private InventoryClient inventoryClient;

	private OrderServiceImpl orderService;

	@BeforeEach
	void setUp() {
		orderService = new OrderServiceImpl(orderRepository, productClient, inventoryClient, new OrderMapper());
	}

	private Order pendingOrder() {
		return new Order("Alice");
	}

	@Test
	void createOrder_happyPath_computesTotalFromFetchedPriceAndPersistsPending() {
		UUID productA = UUID.randomUUID();
		UUID productB = UUID.randomUUID();
		OrderRequest request = new OrderRequest("Alice", List.of(
				new OrderItemRequest(productA, 2),
				new OrderItemRequest(productB, 3)));

		when(productClient.getProduct(productA)).thenReturn(new ProductInfo(productA, "Widget", new BigDecimal("10.00")));
		when(productClient.getProduct(productB)).thenReturn(new ProductInfo(productB, "Gadget", new BigDecimal("5.00")));
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponse response = orderService.createOrder(request);

		assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
		assertThat(response.totalAmount()).isEqualByComparingTo("35.00");
		assertThat(response.items()).hasSize(2);
		verify(inventoryClient).reserveStock(productA, 2);
		verify(inventoryClient).reserveStock(productB, 3);
	}

	@Test
	void createOrder_whenProductNotFound_throwsResourceNotFoundException() {
		UUID productId = UUID.randomUUID();
		OrderRequest request = new OrderRequest("Alice", List.of(new OrderItemRequest(productId, 1)));
		when(productClient.getProduct(productId)).thenThrow(new ResourceNotFoundException("Product not found"));

		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void createOrder_whenInsufficientStock_throwsInvalidOrderException() {
		UUID productId = UUID.randomUUID();
		OrderRequest request = new OrderRequest("Alice", List.of(new OrderItemRequest(productId, 100)));
		when(productClient.getProduct(productId)).thenReturn(new ProductInfo(productId, "Widget", new BigDecimal("10.00")));
		doThrowInsufficientStock(productId, 100);

		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(InvalidOrderException.class);

		verify(orderRepository, never()).save(any(Order.class));
	}

	private void doThrowInsufficientStock(UUID productId, int quantity) {
		org.mockito.Mockito.doThrow(new InsufficientStockException("Insufficient stock"))
				.when(inventoryClient).reserveStock(eq(productId), eq(quantity));
	}

	@Test
	void createOrder_whenDownstreamUnavailable_throwsDownstreamServiceUnavailableException() {
		UUID productId = UUID.randomUUID();
		OrderRequest request = new OrderRequest("Alice", List.of(new OrderItemRequest(productId, 1)));
		when(productClient.getProduct(productId)).thenThrow(new DownstreamServiceUnavailableException("product-service is unavailable"));

		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(DownstreamServiceUnavailableException.class);

		verify(inventoryClient, never()).reserveStock(any(UUID.class), anyInt());
	}

	@Test
	void updateStatus_legalTransition_updatesStatus() {
		UUID id = UUID.randomUUID();
		Order order = pendingOrder();
		when(orderRepository.findById(id)).thenReturn(Optional.of(order));
		when(orderRepository.save(order)).thenReturn(order);

		OrderResponse response = orderService.updateStatus(id, new UpdateStatusRequest(OrderStatus.CONFIRMED));

		assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
	}

	@Test
	void updateStatus_illegalTransition_throwsInvalidOrderException() {
		UUID id = UUID.randomUUID();
		Order order = pendingOrder();
		order.changeStatus(OrderStatus.CANCELLED);
		when(orderRepository.findById(id)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.updateStatus(id, new UpdateStatusRequest(OrderStatus.CONFIRMED)))
				.isInstanceOf(InvalidOrderException.class);

		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void updateStatus_whenNotFound_throwsResourceNotFoundException() {
		UUID id = UUID.randomUUID();
		when(orderRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.updateStatus(id, new UpdateStatusRequest(OrderStatus.CONFIRMED)))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void delete_whenNotConfirmed_deletesOrder() {
		UUID id = UUID.randomUUID();
		Order order = pendingOrder();
		when(orderRepository.findById(id)).thenReturn(Optional.of(order));

		orderService.delete(id);

		verify(orderRepository).deleteById(id);
	}

	@Test
	void delete_whenConfirmed_throwsInvalidOrderException() {
		UUID id = UUID.randomUUID();
		Order order = pendingOrder();
		order.changeStatus(OrderStatus.CONFIRMED);
		when(orderRepository.findById(id)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> orderService.delete(id))
				.isInstanceOf(InvalidOrderException.class);

		verify(orderRepository, never()).deleteById(any(UUID.class));
	}

	@Test
	void delete_whenNotFound_throwsResourceNotFoundException() {
		UUID id = UUID.randomUUID();
		when(orderRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.delete(id))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(orderRepository, never()).deleteById(any(UUID.class));
	}

	@Test
	void findById_whenFound_returnsResponse() {
		UUID id = UUID.randomUUID();
		when(orderRepository.findById(id)).thenReturn(Optional.of(pendingOrder()));

		OrderResponse response = orderService.findById(id);

		assertThat(response.customerName()).isEqualTo("Alice");
	}

	@Test
	void findById_whenNotFound_throwsResourceNotFoundException() {
		UUID id = UUID.randomUUID();
		when(orderRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.findById(id))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining(id.toString());
	}

	@Test
	void findAll_returnsAllMappedOrders() {
		when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(pendingOrder(), pendingOrder()));

		List<OrderResponse> responses = orderService.findAll();

		assertThat(responses).hasSize(2);
	}

}
