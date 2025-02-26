package com.libraryManagement.controller;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.model.Book;
import com.libraryManagement.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody BookDto bookDto) {
        return ResponseEntity.ok(bookService.addBook(bookDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BookDto>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.findByDeletedFalse(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Book>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(bookService.findByCategoryIgnoreCase(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
        return ResponseEntity.ok(bookService.updateBook(id, bookDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteBook(@PathVariable Long id) {
        bookService.softDeleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }
}
