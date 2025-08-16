package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Data
@Entity
@Table(name = "whishlists")
public class WishList extends UserBookCollection{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name; // Optional: e.g., "Favorites", "To Read", etc.

    // Optional helper methods
    public void addBook(Book book) {
        if (book != null && !books.contains(book)) {
            super.addBook(book);
        }
    }

    public void removeBook(Book book) {
        if(book != null){
            super.removeBook(book);
        }
    }
}
