package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.PrePersist;

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
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderItem> items = new HashSet<>();

    private double totalAmount;
    private LocalDateTime orderDate;
    private String status; // e.g., PENDING, SHIPPED, DELIVERED

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
            this.status = "PENDING";
        }
    }
}
