package com.marcosperboni.cloudnative.inventory.application;

import com.marcosperboni.cloudnative.inventory.application.dto.InventoryRequest;
import com.marcosperboni.cloudnative.inventory.application.dto.InventoryResponse;
import com.marcosperboni.cloudnative.inventory.domain.InsufficientStockException;
import com.marcosperboni.cloudnative.inventory.domain.InventoryItem;
import com.marcosperboni.cloudnative.inventory.domain.InventoryRepository;
import com.marcosperboni.cloudnative.inventory.domain.ResourceNotFoundException;
import com.marcosperboni.cloudnative.inventory.infrastructure.ProductClient;
import com.marcosperboni.cloudnative.inventory.infrastructure.ProductServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

	@Mock
	private InventoryRepository inventoryRepository;

	@Mock
	private ProductClient productClient;

	private InventoryService inventoryService;

	@BeforeEach
	void setUp() {
		inventoryService = new InventoryServiceImpl(inventoryRepository, productClient, new InventoryMapper());
	}

	@Test
	void createSavesItemWhenProductExists() {
		UUID productId = UUID.randomUUID();
		InventoryRequest request = new InventoryRequest(productId, 100, "WH-01");
		when(productClient.productExists(productId)).thenReturn(true);
		when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		InventoryResponse response = inventoryService.create(request);

		assertThat(response.productId()).isEqualTo(productId);
		assertThat(response.quantityAvailable()).isEqualTo(100);
		assertThat(response.quantityReserved()).isZero();
		assertThat(response.warehouseLocation()).isEqualTo("WH-01");

		ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
		verify(inventoryRepository).save(captor.capture());
		assertThat(captor.getValue().getProductId()).isEqualTo(productId);
	}

	@Test
	void createThrowsWhenProductDoesNotExist() {
		UUID productId = UUID.randomUUID();
		InventoryRequest request = new InventoryRequest(productId, 100, "WH-01");
		when(productClient.productExists(productId)).thenReturn(false);

		assertThatThrownBy(() -> inventoryService.create(request))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining(productId.toString());

		verify(inventoryRepository, never()).save(any());
	}

	@Test
	void createPropagatesProductServiceUnavailableException() {
		UUID productId = UUID.randomUUID();
		InventoryRequest request = new InventoryRequest(productId, 100, "WH-01");
		when(productClient.productExists(productId))
				.thenThrow(new ProductServiceUnavailableException("down"));

		assertThatThrownBy(() -> inventoryService.create(request))
				.isInstanceOf(ProductServiceUnavailableException.class);

		verify(inventoryRepository, never()).save(any());
	}

	@Test
	void findByIdReturnsItemWhenFound() {
		InventoryItem item = anItem(50, 0);
		when(inventoryRepository.findById(item.getId())).thenReturn(Optional.of(item));

		InventoryResponse response = inventoryService.findById(item.getId());

		assertThat(response.id()).isEqualTo(item.getId());
	}

	@Test
	void findByIdThrowsWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(inventoryRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> inventoryService.findById(id))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void findAllReturnsAllItems() {
		InventoryItem item1 = anItem(10, 0);
		InventoryItem item2 = anItem(20, 0);
		when(inventoryRepository.findAll()).thenReturn(List.of(item1, item2));

		List<InventoryResponse> responses = inventoryService.findAll();

		assertThat(responses).hasSize(2);
	}

	@Test
	void updateModifiesExistingItem() {
		InventoryItem item = anItem(50, 0);
		when(inventoryRepository.findById(item.getId())).thenReturn(Optional.of(item));
		when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		InventoryRequest request = new InventoryRequest(item.getProductId(), 75, "WH-02");
		InventoryResponse response = inventoryService.update(item.getId(), request);

		assertThat(response.quantityAvailable()).isEqualTo(75);
		assertThat(response.warehouseLocation()).isEqualTo("WH-02");
	}

	@Test
	void updateThrowsWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(inventoryRepository.findById(id)).thenReturn(Optional.empty());

		InventoryRequest request = new InventoryRequest(UUID.randomUUID(), 10, "WH-01");

		assertThatThrownBy(() -> inventoryService.update(id, request))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deleteRemovesExistingItem() {
		UUID id = UUID.randomUUID();
		when(inventoryRepository.existsById(id)).thenReturn(true);

		inventoryService.delete(id);

		verify(inventoryRepository, times(1)).deleteById(id);
	}

	@Test
	void deleteThrowsWhenNotFound() {
		UUID id = UUID.randomUUID();
		when(inventoryRepository.existsById(id)).thenReturn(false);

		assertThatThrownBy(() -> inventoryService.delete(id))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(inventoryRepository, never()).deleteById(any());
	}

	@Test
	void reserveStockMovesQuantityWhenEnoughStock() {
		InventoryItem item = anItem(100, 10);
		when(inventoryRepository.findById(item.getId())).thenReturn(Optional.of(item));
		when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		InventoryResponse response = inventoryService.reserveStock(item.getId(), 30);

		assertThat(response.quantityAvailable()).isEqualTo(70);
		assertThat(response.quantityReserved()).isEqualTo(40);
	}

	@Test
	void reserveStockThrowsWhenInsufficientStock() {
		InventoryItem item = anItem(10, 0);
		when(inventoryRepository.findById(item.getId())).thenReturn(Optional.of(item));

		assertThatThrownBy(() -> inventoryService.reserveStock(item.getId(), 20))
				.isInstanceOf(InsufficientStockException.class);

		verify(inventoryRepository, never()).save(any());
	}

	@Test
	void reserveStockThrowsWhenItemNotFound() {
		UUID id = UUID.randomUUID();
		when(inventoryRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> inventoryService.reserveStock(id, 10))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void reserveStockByProductMovesQuantityWhenEnoughStock() {
		InventoryItem item = anItem(100, 10);
		when(inventoryRepository.findByProductId(item.getProductId())).thenReturn(Optional.of(item));
		when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		InventoryResponse response = inventoryService.reserveStockByProduct(item.getProductId(), 30);

		assertThat(response.quantityAvailable()).isEqualTo(70);
		assertThat(response.quantityReserved()).isEqualTo(40);
	}

	@Test
	void reserveStockByProductThrowsWhenInsufficientStock() {
		InventoryItem item = anItem(10, 0);
		when(inventoryRepository.findByProductId(item.getProductId())).thenReturn(Optional.of(item));

		assertThatThrownBy(() -> inventoryService.reserveStockByProduct(item.getProductId(), 20))
				.isInstanceOf(InsufficientStockException.class);

		verify(inventoryRepository, never()).save(any());
	}

	@Test
	void reserveStockByProductThrowsWhenNoInventoryForProduct() {
		UUID productId = UUID.randomUUID();
		when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> inventoryService.reserveStockByProduct(productId, 10))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	private InventoryItem anItem(int available, int reserved) {
		return new InventoryItem(UUID.randomUUID(), available, reserved, "WH-01", Instant.now());
	}
}
