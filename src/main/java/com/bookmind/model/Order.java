package com.bookmind.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    private double totalAmount;
    private LocalDateTime orderDate;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // e.g., PENDING, SHIPPED, DELIVERED
    private String shippingAddress;

    public void addOrderItem(OrderItem orderItem){
        if(orderItem != null){
            items.add(orderItem);
            orderItem.setOrder(this);
            totalAmount += orderItem.getPrice() * orderItem.getQuantity();
        }
    }

    public void removeOrderItem(OrderItem orderItem){
        if(orderItem != null){
            items.remove(orderItem);
            orderItem.setOrder(null);
            totalAmount -= orderItem.getPrice() * orderItem.getQuantity();
        }
    }
    
    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
