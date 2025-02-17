package com.libraryManagement.service;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    Book addBook(BookDto bookDto);
    Book getBookById(Long id);
    Page<BookDto> findByDeletedFalse(Pageable pageable);
    List<Book> searchBooks(String keyword);
    List<Book> findByCategoryIgnoreCase(String category);
    Book updateBook(Long id, BookDto bookDto);
    void softDeleteBook(Long id);
}
