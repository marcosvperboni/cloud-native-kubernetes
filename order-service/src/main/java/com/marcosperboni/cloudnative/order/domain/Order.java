package com.marcosperboni.cloudnative.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "customer_name", nullable = false, length = 150)
	private String customerName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<OrderItem> items = new ArrayList<>();

	protected Order() {
		// JPA
	}

	public Order(String customerName) {
		this.customerName = customerName;
		this.status = OrderStatus.PENDING;
		this.totalAmount = BigDecimal.ZERO;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void addItem(OrderItem item) {
		item.assignOrder(this);
		items.add(item);
	}

	public void recalculateTotal() {
		if (items.isEmpty()) {
			throw InvalidOrderException.emptyItems();
		}
		this.totalAmount = items.stream()
				.map(OrderItem::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void changeStatus(OrderStatus newStatus) {
		if (!status.canTransitionTo(newStatus)) {
			throw InvalidOrderException.illegalTransition(status, newStatus);
		}
		this.status = newStatus;
	}

	public UUID getId() {
		return id;
	}

	public String getCustomerName() {
		return customerName;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<OrderItem> getItems() {
		return items;
	}

}
