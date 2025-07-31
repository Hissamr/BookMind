package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    @Column(length = 2000)
    private String description;
    private String genre;
    private String language;
    private String publisher;
    private int publicationYear;
    private double price;
    private boolean available;
    private int pages;
    private double rating;
    private String coverImageUrl;
    private String isbn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
