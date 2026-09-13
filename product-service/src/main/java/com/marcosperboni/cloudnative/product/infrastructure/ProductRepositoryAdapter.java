package com.marcosperboni.cloudnative.product.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.marcosperboni.cloudnative.product.domain.Product;
import com.marcosperboni.cloudnative.product.domain.ProductRepository;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

	private final ProductJpaRepository jpaRepository;

	public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Product save(Product product) {
		return jpaRepository.save(product);
	}

	@Override
	public Optional<Product> findById(UUID id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<Product> findAllByOrderByNameAsc() {
		return jpaRepository.findAllByOrderByNameAsc();
	}

	@Override
	public boolean existsById(UUID id) {
		return jpaRepository.existsById(id);
	}

	@Override
	public void deleteById(UUID id) {
		jpaRepository.deleteById(id);
	}

}
