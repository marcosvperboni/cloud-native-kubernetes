package com.marcosperboni.cloudnative.product.application;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcosperboni.cloudnative.product.application.dto.ProductRequest;
import com.marcosperboni.cloudnative.product.application.dto.ProductResponse;
import com.marcosperboni.cloudnative.product.domain.Product;
import com.marcosperboni.cloudnative.product.domain.ProductRepository;
import com.marcosperboni.cloudnative.product.domain.ResourceNotFoundException;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	@Transactional
	public ProductResponse create(ProductRequest request) {
		Product product = new Product(request.name(), request.description(), request.price(), request.stockQuantity());
		return ProductMapper.toResponse(productRepository.save(product));
	}

	@Override
	@Cacheable(value = "products", key = "#id")
	@Transactional(readOnly = true)
	public ProductResponse findById(UUID id) {
		return productRepository.findById(id)
				.map(ProductMapper::toResponse)
				.orElseThrow(() -> ResourceNotFoundException.forProduct(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponse> findAll() {
		return productRepository.findAllByOrderByNameAsc().stream()
				.map(ProductMapper::toResponse)
				.toList();
	}

	@Override
	@CacheEvict(value = "products", key = "#id")
	@Transactional
	public ProductResponse update(UUID id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> ResourceNotFoundException.forProduct(id));
		product.update(request.name(), request.description(), request.price(), request.stockQuantity());
		return ProductMapper.toResponse(productRepository.save(product));
	}

	@Override
	@CacheEvict(value = "products", key = "#id")
	@Transactional
	public void delete(UUID id) {
		if (!productRepository.existsById(id)) {
			throw ResourceNotFoundException.forProduct(id);
		}
		productRepository.deleteById(id);
	}

}
