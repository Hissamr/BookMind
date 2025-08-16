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
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    @Column(length = 2000)
    private String description;
    private String genre;

    @ManyToMany
    @JoinTable(name = "book_categories",
               joinColumns = @JoinColumn(name = "book_id"),
               inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    private String language;
    private String publisher;
    private int publicationYear;
    private double price;
    private Boolean available;
    private int pages;
    private double averageRating;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
    private String coverImageUrl;
    private String isbn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addCategory(Category category) {
        if(category != null){
            categories.add(category);
            category.getBooks().add(this);
        }
    }

    public void removeCategory(Category category) {
        if(category != null){
            categories.remove(category);
            category.getBooks().remove(this);
        }
    }

    public void addReview(Review review) {
        if(review != null){
            reviews.add(review);
            review.setBook(this);
        }
    }

    public void removeReview(Review review) {
        if(review != null){
            reviews.remove(review);
            review.setBook(null);
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
