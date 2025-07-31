package com.bookmind.model;

import jakarta.persistence.*;
import java.util.*;

@MappedSuperclass
public class UserBookCollection implements BookCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    protected User user;

    @ManyToMany
    @JoinTable(
            name = "collection_books",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    protected List<Book> books = new ArrayList<>();

    @Override
    public void addBook(Book book) {
        if (book != null && !books.contains(book)) {
            books.add(book);
        }
    }

    @Override
    public void removeBook(Book book) {
        if (book != null) {
            books.remove(book);
        }
    }

    @Override
    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }

}
