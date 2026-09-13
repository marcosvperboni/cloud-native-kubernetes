package com.marcosperboni.cloudnative.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.marcosperboni.cloudnative.product.application.dto.ProductRequest;
import com.marcosperboni.cloudnative.product.application.dto.ProductResponse;
import com.marcosperboni.cloudnative.product.domain.Product;
import com.marcosperboni.cloudnative.product.domain.ProductRepository;
import com.marcosperboni.cloudnative.product.domain.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

	@Mock
	private ProductRepository productRepository;

	private ProductServiceImpl productService;

	@BeforeEach
	void setUp() {
		productService = new ProductServiceImpl(productRepository);
	}

	private Product sampleProduct() {
		return new Product("Keyboard", "Mechanical keyboard", new BigDecimal("199.90"), 10);
	}

	@Test
	void create_savesAndReturnsResponse() {
		ProductRequest request = new ProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("199.90"), 10);
		when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ProductResponse response = productService.create(request);

		assertThat(response.name()).isEqualTo("Keyboard");
		assertThat(response.price()).isEqualByComparingTo("199.90");
		assertThat(response.stockQuantity()).isEqualTo(10);
		assertThat(response.active()).isTrue();
		verify(productRepository).save(any(Product.class));
	}

	@Test
	void findById_whenFound_returnsResponse() {
		Product product = sampleProduct();
		UUID id = UUID.randomUUID();
		when(productRepository.findById(id)).thenReturn(Optional.of(product));

		ProductResponse response = productService.findById(id);

		assertThat(response.name()).isEqualTo("Keyboard");
	}

	@Test
	void findById_whenNotFound_throwsResourceNotFoundException() {
		UUID id = UUID.randomUUID();
		when(productRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.findById(id))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining(id.toString());
	}

	@Test
	void findAll_returnsAllMappedProducts() {
		when(productRepository.findAllByOrderByNameAsc()).thenReturn(List.of(sampleProduct(), sampleProduct()));

		List<ProductResponse> responses = productService.findAll();

		assertThat(responses).hasSize(2);
	}

	@Test
	void update_whenFound_updatesAndReturnsResponse() {
		Product product = sampleProduct();
		UUID id = UUID.randomUUID();
		ProductRequest request = new ProductRequest("Keyboard Pro", "Updated", new BigDecimal("249.90"), 5);
		when(productRepository.findById(id)).thenReturn(Optional.of(product));
		when(productRepository.save(product)).thenReturn(product);

		ProductResponse response = productService.update(id, request);

		assertThat(response.name()).isEqualTo("Keyboard Pro");
		assertThat(response.price()).isEqualByComparingTo("249.90");
		assertThat(response.stockQuantity()).isEqualTo(5);
	}

	@Test
	void update_whenNotFound_throwsResourceNotFoundException() {
		UUID id = UUID.randomUUID();
		ProductRequest request = new ProductRequest("Keyboard Pro", "Updated", new BigDecimal("249.90"), 5);
		when(productRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.update(id, request))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	void delete_whenFound_deletesProduct() {
		UUID id = UUID.randomUUID();
		when(productRepository.existsById(id)).thenReturn(true);

		productService.delete(id);

		verify(productRepository, times(1)).deleteById(id);
	}

	@Test
	void delete_whenNotFound_throwsResourceNotFoundException() {
		UUID id = UUID.randomUUID();
		when(productRepository.existsById(id)).thenReturn(false);

		assertThatThrownBy(() -> productService.delete(id))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(productRepository, never()).deleteById(any(UUID.class));
	}

}
