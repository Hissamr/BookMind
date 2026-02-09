package com.bookmind.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import com.bookmind.model.Book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for interacting with OpenAI API.
 * Handles book summary generation and other AI-powered features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String BOOK_SUMMARY_PROMPT = """
            You are a helpful book expert. Generate a compelling and informative summary for the following book.
            The summary should be engaging, highlight key themes, and help readers decide if they want to read it.
            Keep the summary between 150-250 words.
            
            Book Details:
            - Title: {title}
            - Author: {author}
            - Genre: {genre}
            - Description: {description}
            - Publication Year: {publicationYear}
            - Pages: {pages}
            
            Please provide a well-structured summary that includes:
            1. A brief overview of what the book is about
            2. Key themes or topics covered
            3. Who would enjoy this book
            
            Summary:
            """;

    /**
     * Generate an AI summary for a book.
     *
     * @param book the book to generate a summary for
     * @return the generated summary
     */
    public String generateBookSummary(Book book) {
        log.info("Generating AI summary for book: {} by {}", book.getTitle(), book.getAuthor());

        try {
            PromptTemplate promptTemplate = new PromptTemplate(BOOK_SUMMARY_PROMPT);
            promptTemplate.add("title", book.getTitle() != null ? book.getTitle() : "Unknown");
            promptTemplate.add("author", book.getAuthor() != null ? book.getAuthor() : "Unknown");
            promptTemplate.add("genre", book.getGenre() != null ? book.getGenre() : "Not specified");
            promptTemplate.add("description", book.getDescription() != null ? book.getDescription() : "No description available");
            promptTemplate.add("publicationYear", String.valueOf(book.getPublicationYear()));
            promptTemplate.add("pages", String.valueOf(book.getPages()));

            Prompt prompt = promptTemplate.create();

            ChatClient chatClient = chatClientBuilder.build();
            String summary = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("Successfully generated summary for book ID: {}", book.getId());
            return summary;

        } catch (Exception e) {
            log.error("Error generating summary for book ID {}: {}", book.getId(), e.getMessage());
            throw new RuntimeException("Failed to generate book summary: " + e.getMessage(), e);
        }
    }
}
