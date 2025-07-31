package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "carts")
public class Cart extends UserBookCollection {

    private double totalPrice;

    private boolean checkedOut = false;

    public void addBook(Book book) {
        if(book != null) {
            super.addBook(book);
            totalPrice += book.getPrice(); // Update total price when a book is added
        }
    }

    public void removeBook(Book book) {
        if(book != null) {
            super.removeBook(book);
            totalPrice -= book.getPrice(); // Update total price when a book is removed
        }
    }

}
