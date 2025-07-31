package com.bookmind.model;

import java.util.List;

public interface BookCollection {

    void addBook(Book book);
    void removeBook(Book book);
    List<Book> getBooks();

}
