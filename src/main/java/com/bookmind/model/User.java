package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    private boolean enabled = true;
    private boolean emailVerified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WishList> wishlists = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public void addReview(Review review){
        if(review != null){
            reviews.add(review);
            review.setUser(this);
        }
    }

    public void removeReview(Review review){
        if(review != null){
            reviews.remove(review);
            review.setUser(null);
        }
    }

    public void addWishList(WishList wishList) {
        if (wishList != null) {
            wishlists.add(wishList);
            wishList.setUser(this);
        }
    }

    public void removeWishList(WishList wishList){
        if (wishList != null) {
            wishlists.remove(wishList);
            wishList.setUser(null);
        }
    }

    public void addOrder(Order order) {
        if (order != null) {
            orders.add(order);
            order.setUser(this);
        }
    }

    public void removeOrder(Order order) {
        if (order != null) {
            orders.remove(order);
            order.setUser(null);
        }
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
