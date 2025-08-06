package com.bookmind.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;

    private int rating; //eg: 1 - 5
    private String comment;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
