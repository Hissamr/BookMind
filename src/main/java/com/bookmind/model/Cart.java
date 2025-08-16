package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "carts")
public class Cart extends UserBookCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double totalPrice;

    private boolean checkedOut = false;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
