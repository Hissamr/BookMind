package com.bookmind.service;

import com.bookmind.model.Book;
import com.bookmind.model.Category;

import com.bookmind.model.Review;
import com.bookmind.repository.BookRepository;
import com.bookmind.repository.CategoryRepository;
import com.bookmind.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Get all the Books from the database.
     * @return List of all Books
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Get a Book by its ID.
     * @param id ID of the Book
     * @return Book object
     * @throws RuntimeException if the Book is not found
     */
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
    }

    /**
     * Save a new Book to the database.
     * @param book Book object to be saved
     * @return Saved Book object
     */
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Update an existing Book in the database.
     * @param id ID of the Book to be updated
     * @param book Updated Book object
     * @return Updated Book object
     * @throws RuntimeException if the Book is not found
     */
    public Book updateBook(Long id, Book book) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with ID: " + id);
        }
        book.setId(id); // Ensure the ID is set for the update
        return bookRepository.save(book);
    }

    /**
     * Delete a Book by its ID.
     * @param id ID of the Book to be deleted
     * @throws RuntimeException if the Book is not found
     */
    public void deleteBook(Long id) {
        if(!bookRepository.existsById(id)){
            throw new RuntimeException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
    }

    /**
     * Add a Category to a Book.
     * @param bookId ID of the Book
     * @param categoryId ID of the Category
     * @throws RuntimeException if the Book or Category is not found
     */
    public void addCategoryToBook(Long bookId, Long categoryId) {
        Book book = getBookById(bookId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        book.addCategory(category);
        bookRepository.save(book);
    }

    /**
     * Remove a Category from a Book.
     * @param bookId ID of the Book
     * @param categoryId ID of the Category
     * @throws RuntimeException if the Book or Category is not found
     */
    public void removeCategoryFromBook(Long bookId, Long categoryId) {
        Book book = getBookById(bookId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        book.removeCategory(category);
        bookRepository.save(book);
    }

    /**
     * Add a Review to a Book.
     * @param bookId ID of the Book
     * @param reviewId ID of the Review
     * @throws RuntimeException if the Book or Review is not found
     */
    public void addReviewToBook(Long bookId, Long reviewId) {
        Book book = getBookById(bookId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        book.addReview(review);
        updateBookAverageRating(book);
        bookRepository.save(book);
    }

    /**
     * Remove a Review from a Book.
     * @param bookId ID of the Book
     * @param reviewId ID of the Review
     * @throws RuntimeException if the Book or Review is not found
     */
    public void removeReviewFromBook(Long bookId, Long reviewId) {
        Book book = getBookById(bookId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        book.removeReview(review);
        updateBookAverageRating(book);
        bookRepository.save(book);
    }
    
    /**
     * Updates the average rating of a book based on its reviews.
     * @param book The book whose average rating needs to be updated
     */
    private void updateBookAverageRating(Book book) {
        if (book.getReviews().isEmpty()) {
            book.setAverageRating(0.0);
            return;
        }
        
        double sum = 0;
        for (Review review : book.getReviews()) {
            sum += review.getRating();
        }
        
        book.setAverageRating(sum / book.getReviews().size());
    }

    /**
     * Search for books by title, author, and/or genre.
     * @param title Optional title to search for
     * @param author Optional author to search for
     * @param genre Optional genre to search for
     * @return List of matching books
     */
    public List<Book> searchBooks(String title, String author, String genre) {
        return bookRepository.searchBooks(title, author, genre, null, null, null, null, null);
    }
    
    /**
     * Advanced search for books with multiple criteria.
     * @param title Optional title to search for
     * @param author Optional author to search for
     * @param genre Optional genre to search for
     * @param description Optional description to search for
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param minRating Optional minimum rating
     * @param available Optional availability status
     * @return List of matching books
     */
    public List<Book> advancedSearchBooks(
            String title, String author, String genre, String description,
            Double minPrice, Double maxPrice, Double minRating, Boolean available) {
        return bookRepository.searchBooks(
            title, author, genre, description, minPrice, maxPrice, minRating, available);
    }
    
    /**
     * Get books by category ID.
     * @param categoryId ID of the Category
     * @return List of books in the category
     * @throws RuntimeException if the Category is not found
     */
    public List<Book> getBooksByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
                
        return bookRepository.findByCategory(category);
    }

    /**
     * Get books that are available.
     * @return List of available books
     */
    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableTrue();
    }
    
    /**
     * Get books with rating greater than or equal to the specified minimum.
     * @param minRating Minimum rating threshold
     * @return List of books with rating >= minRating
     */
    public List<Book> getTopRatedBooks(Double minRating) {
        return bookRepository.findByAverageRatingGreaterThanEqual(minRating);
    }
    
    /**
     * Get books with price less than or equal to the specified maximum.
     * @param maxPrice Maximum price threshold
     * @return List of books with price <= maxPrice
     */
    public List<Book> getBooksByPriceRange(Double maxPrice) {
        return bookRepository.findByPriceLessThanEqual(maxPrice);
    }
    
    /**
     * Advanced search for books with pagination support.
     * @param title Optional title to search for
     * @param author Optional author to search for
     * @param genre Optional genre to search for
     * @param description Optional description to search for
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param minRating Optional minimum rating
     * @param available Optional availability status
     * @param page Page number (0-based)
     * @param size Items per page
     * @param sortBy Field to sort by
     * @param direction Sort direction (asc or desc)
     * @return Page of matching books
     */
    public Page<Book> searchBooksWithPagination(
            String title, String author, String genre, String description,
            Double minPrice, Double maxPrice, Double minRating, Boolean available,
            int page, int size, String sortBy, String direction) {
            
        Sort sort = Sort.by(sortBy);
        sort = "desc".equalsIgnoreCase(direction) ? sort.descending() : sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return bookRepository.searchBooksWithPagination(
            title, author, genre, description, minPrice, maxPrice, minRating, available, pageable);
    }
}
