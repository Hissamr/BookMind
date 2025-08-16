package com.bookmind.repository;

import com.bookmind.model.Book;
import com.bookmind.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Find books by title (case-insensitive)
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Find books by author (case-insensitive)
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Find books by genre (case-insensitive)
    List<Book> findByGenreContainingIgnoreCase(String genre);
    
    // Find books with price less than or equal to the specified amount
    List<Book> findByPriceLessThanEqual(double price);
    
    // Find books by publication year
    List<Book> findByPublicationYear(int year);
    
    // Find books that are available
    List<Book> findByAvailableTrue();
    
    // Find books with average rating greater than or equal to the specified value
    List<Book> findByAverageRatingGreaterThanEqual(double rating);
    
    // Find books belonging to a specific category
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c = :category")
    List<Book> findByCategory(@Param("category") Category category);
    
    // Advanced search with multiple criteria
    @Query(value = """
        SELECT DISTINCT * 
        FROM books b
        WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
        AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%')))
        AND (:description IS NULL OR LOWER(b.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:minPrice IS NULL OR b.price >= :minPrice)
        AND (:maxPrice IS NULL OR b.price <= :maxPrice)
        AND (:minRating IS NULL OR b.average_rating >= :minRating)
        AND (:available IS NULL OR b.available = :available)
    """, nativeQuery = true)
    List<Book> searchBooks(
        @Param("title") String title,
        @Param("author") String author,
        @Param("genre") String genre,
        @Param("description") String description,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("minRating") Double minRating,
        @Param("available") Boolean available
    );

    // Pageable versions of the search methods
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    Page<Book> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
    Page<Book> findByPriceLessThanEqual(double price, Pageable pageable);
    Page<Book> findByAvailableTrue(Pageable pageable);
    Page<Book> findByAverageRatingGreaterThanEqual(double rating, Pageable pageable);
    
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c = :category")
    Page<Book> findByCategory(@Param("category") Category category, Pageable pageable);
    
    @Query(value = "SELECT DISTINCT * FROM books b " +
           "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
           "AND (:description IS NULL OR LOWER(b.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR b.price <= :maxPrice) " +
           "AND (:minRating IS NULL OR b.average_rating >= :minRating) " +
           "AND (:available IS NULL OR b.available = :available)",
           countQuery = "SELECT count(DISTINCT b.id) FROM books b " +
           "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
           "AND (:description IS NULL OR LOWER(b.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR b.price <= :maxPrice) " +
           "AND (:minRating IS NULL OR b.average_rating >= :minRating) " +
           "AND (:available IS NULL OR b.available = :available)",
           nativeQuery = true)
    Page<Book> searchBooksWithPagination(
        @Param("title") String title,
        @Param("author") String author,
        @Param("genre") String genre,
        @Param("description") String description,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("minRating") Double minRating,
        @Param("available") Boolean available,
        Pageable pageable
    );
}
