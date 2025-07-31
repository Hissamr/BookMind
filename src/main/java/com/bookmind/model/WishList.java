package com.bookmind.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "whishlists")
public class WishList extends UserBookCollection{

    private String name; // Optional: e.g., "Favorites", "To Read", etc.

    // Optional helper methods
    public void addBook(Book book) {
        if (book != null && !books.contains(book)) {
            super.addBook(book);
        }
    }

    public void removeBook(Book book) {
        super.removeBook(book);
    }
}
