package com.bookmind.controller;

import com.bookmind.model.Book;
import com.bookmind.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
       return new ResponseEntity<>(bookService.getAllBooks(), HttpStatus.OK);
    }

    @GetMapping("/book/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/book")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            Book savedBook = bookService.addBook(book);
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        }
        catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding book: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Error adding book: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/book/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error updating book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/book/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error deleting book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/book/{bookId}/category/{categoryId}")
    public ResponseEntity<?> addCategoryToBook(@PathVariable Long bookId, @PathVariable Long categoryId) {
        try {
            bookService.addCategoryToBook(bookId, categoryId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding category to book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/book/{bookId}/category/{categoryId}")
    public ResponseEntity<?> removeCategoryFromBook(@PathVariable Long bookId, @PathVariable Long categoryId) {
        try {
            bookService.removeCategoryFromBook(bookId, categoryId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error removing category from book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/book/{bookId}/review/{reviewId}")
    public ResponseEntity<?> addReviewToBook(@PathVariable Long bookId, @PathVariable Long reviewId) {
        try {
            bookService.addReviewToBook(bookId, reviewId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error adding review to book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/book/{bookId}/review/{reviewId}")
    public ResponseEntity<?> removeReviewFromBook(@PathVariable Long bookId, @PathVariable Long reviewId) {
        try {
            bookService.removeReviewFromBook(bookId, reviewId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error removing review from book: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/books/search")
    public ResponseEntity<?> searchBooks(@RequestParam(required = false) String title,
                                         @RequestParam(required = false) String author,
                                         @RequestParam(required = false) String genre) {
        try {
            List<Book> books = bookService.searchBooks(title, author, genre);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching for books: " + e.getMessage(), 
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/advanced-search")
    public ResponseEntity<?> advancedSearchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean available) {
        try {
            List<Book> books = bookService.advancedSearchBooks(
                title, author, genre, description, minPrice, maxPrice, minRating, available);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching for books: " + e.getMessage(), 
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/category/{categoryId}")
    public ResponseEntity<?> getBooksByCategory(@PathVariable Long categoryId) {
        try {
            List<Book> books = bookService.getBooksByCategory(categoryId);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error finding books: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/books/available")
    public ResponseEntity<?> getAvailableBooks() {
        try {
            List<Book> books = bookService.getAvailableBooks();
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding available books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/top-rated")
    public ResponseEntity<?> getTopRatedBooks(@RequestParam(required = false, defaultValue = "4.0") Double minRating) {
        try {
            List<Book> books = bookService.getTopRatedBooks(minRating);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding top-rated books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/price-range")
    public ResponseEntity<?> getBooksByPriceRange(@RequestParam Double maxPrice) {
        try {
            List<Book> books = bookService.getBooksByPriceRange(maxPrice);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error finding books by price: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/books/search/paged")
    public ResponseEntity<?> searchBooksWithPagination(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<Book> books = bookService.searchBooksWithPagination(
                title, author, genre, description, minPrice, maxPrice, minRating, available,
                page, size, sortBy, direction);
                
            Map<String, Object> response = new HashMap<>();
            response.put("books", books.getContent());
            response.put("currentPage", books.getNumber());
            response.put("totalItems", books.getTotalElements());
            response.put("totalPages", books.getTotalPages());
                
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error searching for books: " + e.getMessage(), 
                                      HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
