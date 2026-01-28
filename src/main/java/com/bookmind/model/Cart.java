package com.bookmind.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double totalPrice;

    private boolean checkedOut = false;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Add a CartItem to the cart
     */
    public void addCartItem(CartItem cartItem) {
        if (cartItem != null) {
            items.add(cartItem);
            cartItem.setCart(this);
            totalPrice += cartItem.getPrice() * cartItem.getQuantity();
        }
    }

    /**
     * Remove a CartItem from the cart
     */
    public void removeCartItem(CartItem cartItem) {
        if (cartItem != null) {
            items.remove(cartItem);
            cartItem.setCart(null);
            totalPrice -= cartItem.getPrice() * cartItem.getQuantity();
        }
    }

    /**
     * Clear all items from the cart
     */
    public void clearCart() {
        items.forEach(item -> item.setCart(null));
        items.clear();
        totalPrice = 0.0;
    }

    /**
     * Recalculate total price from all items
     */
    public void recalculateTotalPrice() {
        this.totalPrice = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
