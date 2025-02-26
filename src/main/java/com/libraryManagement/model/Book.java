package com.libraryManagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private int publishedYear;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
