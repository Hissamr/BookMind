package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;
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

}
