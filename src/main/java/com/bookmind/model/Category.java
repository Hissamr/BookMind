package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addBook(Book book) {
        if(book != null){
            books.add(book);
            book.getCategories().add(this); // Ensure bidirectional relationship
        }
    }

    public void removeBook(Book book) {
        if(book != null){
            books.remove(book);
            book.getCategories().remove(this); // Ensure bidirectional relationship
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
