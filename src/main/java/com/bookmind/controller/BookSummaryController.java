package com.bookmind.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookmind.dto.BookSummaryResponse;
import com.bookmind.service.BookSummaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for AI-powered book summary operations.
 * 
 * PUBLIC: Getting a book summary (auto-generates if not cached).
 * ADMIN ONLY: Force regeneration, batch generation, and deletion.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/books")
public class BookSummaryController {

    private final BookSummaryService bookSummaryService;

    /**
     * Get the AI-generated summary for a book.
     * Returns cached summary if available, otherwise generates a new one.
     *
     * @param bookId the book ID
     * @return BookSummaryResponse with the summary
     */
    @GetMapping("/{bookId}/summary")
    public ResponseEntity<BookSummaryResponse> getBookSummary(@PathVariable Long bookId) {
        log.info("Fetching summary for book ID: {}", bookId);
        BookSummaryResponse response = bookSummaryService.getBookSummary(bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * Force regenerate the AI summary for a book (Admin only).
     * Overwrites existing summary.
     *
     * @param bookId the book ID
     * @return BookSummaryResponse with the new summary
     */
    @PostMapping("/{bookId}/summary/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookSummaryResponse> generateBookSummary(@PathVariable Long bookId) {
        log.info("Admin requesting summary regeneration for book ID: {}", bookId);
        BookSummaryResponse response = bookSummaryService.generateBookSummary(bookId);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate summaries for all books that don't have one (Admin only).
     * Batch operation.
     *
     * @return count of summaries generated
     */
    @PostMapping("/summaries/generate-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateAllMissingSummaries() {
        log.info("Admin requesting batch summary generation");
        int count = bookSummaryService.generateMissingSummaries();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "summariesGenerated", count,
                "message", String.format("Successfully generated %d summaries", count)
        ));
    }

    /**
     * Delete the AI summary for a book (Admin only).
     *
     * @param bookId the book ID
     * @return success message
     */
    @DeleteMapping("/{bookId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBookSummary(@PathVariable Long bookId) {
        log.info("Admin deleting summary for book ID: {}", bookId);
        bookSummaryService.deleteSummary(bookId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Summary deleted successfully"
        ));
    }
}
