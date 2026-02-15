package com.bookmind.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookmind.dto.BookSummaryResponse;
import com.bookmind.exception.BookNotFoundException;
import com.bookmind.model.Book;
import com.bookmind.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing book summaries.
 * Handles generation, caching, and retrieval of AI-generated book summaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookSummaryService {

    private final BookRepository bookRepository;
    private final GoogleAiClient googleAiClient;

    /**
     * Get the summary for a book. Returns cached summary if available,
     * otherwise generates a new one.
     *
     * @param bookId the ID of the book
     * @return BookSummaryResponse containing the summary
     */
    @Transactional
    public BookSummaryResponse getBookSummary(Long bookId) {
        log.info("Getting summary for book ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        // Check if summary already exists
        if (book.getAiSummary() != null && !book.getAiSummary().isBlank()) {
            log.debug("Returning cached summary for book ID: {}", bookId);
            return BookSummaryResponse.builder()
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .summary(book.getAiSummary())
                    .generatedAt(book.getSummaryGeneratedAt())
                    .cached(true)
                    .message("Summary retrieved from cache")
                    .build();
        }

        // Generate new summary
        return generateAndSaveSummary(book);
    }

    /**
     * Generate a new summary for a book, regardless of whether one exists.
     * Admin only - used to regenerate/update summaries.
     *
     * @param bookId the ID of the book
     * @return BookSummaryResponse containing the new summary
     */
    @Transactional
    public BookSummaryResponse generateBookSummary(Long bookId) {
        log.info("Generating new summary for book ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        return generateAndSaveSummary(book);
    }

    /**
     * Generate summaries for all books that don't have one.
     * Admin only - batch operation.
     *
     * @return number of summaries generated
     */
    @Transactional
    public int generateMissingSummaries() {
        log.info("Starting batch generation of missing summaries");

        var booksWithoutSummary = bookRepository.findAll().stream()
                .filter(book -> book.getAiSummary() == null || book.getAiSummary().isBlank())
                .toList();

        int successCount = 0;
        for (Book book : booksWithoutSummary) {
            try {
                generateAndSaveSummary(book);
                successCount++;
                log.debug("Generated summary for book: {}", book.getTitle());
            } catch (Exception e) {
                log.error("Failed to generate summary for book ID {}: {}", book.getId(), e.getMessage());
            }
        }

        log.info("Batch generation complete. Generated {} summaries out of {} books", 
                successCount, booksWithoutSummary.size());
        return successCount;
    }

    /**
     * Delete the summary for a book.
     *
     * @param bookId the ID of the book
     */
    @Transactional
    public void deleteSummary(Long bookId) {
        log.info("Deleting summary for book ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        book.setAiSummary(null);
        book.setSummaryGeneratedAt(null);
        bookRepository.save(book);

        log.info("Summary deleted for book ID: {}", bookId);
    }

    /**
     * Internal method to generate and save a summary for a book.
     */
    private BookSummaryResponse generateAndSaveSummary(Book book) {
        String prompt = """
            You are a helpful book expert. Generate a compelling and informative summary for the following book.
            The summary should be engaging, highlight key themes, and help readers decide if they want to read it.
            Keep the summary between 150-250 words.
            
            Book Details:
            - Title: %s
            - Author: %s
            - Genre: %s
            - Description: %s
            - Publication Year: %s
            - Pages: %s
            
            Please provide a well-structured summary that includes:
            1. A brief overview of what the book is about
            2. Key themes or topics covered
            3. Who would enjoy this book
            
            Summary:
            """.formatted(book.getTitle(), book.getAuthor(), book.getGenre(), book.getDescription(), book.getPublicationYear(), book.getPages());
            
        String summary = googleAiClient.generateBookSummary(prompt);
        LocalDateTime generatedAt = LocalDateTime.now();

        book.setAiSummary(summary);
        book.setSummaryGeneratedAt(generatedAt);
        bookRepository.save(book);

        log.info("Summary saved for book ID: {}", book.getId());

        return BookSummaryResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .summary(summary)
                .generatedAt(generatedAt)
                .cached(false)
                .message("Summary generated successfully")
                .build();
    }
}
