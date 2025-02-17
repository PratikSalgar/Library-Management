package com.libraryManagement.controller;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.model.Book;
import com.libraryManagement.service.BookService;
import com.libraryManagement.service.Impl.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final FileStorageService fileStorageService;

    public BookController(BookService bookService, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
    }

    // 1️⃣ Upload Book Image
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file); // Store the file and get the file name
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/books/images/")
                .path(fileName)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", fileUrl);

        return ResponseEntity.ok(response);
    }


    // 2️⃣ Add New Book with Image
    @PostMapping
    public ResponseEntity<Book> addBook(@Valid @RequestBody BookDto bookDto) {
        return ResponseEntity.ok(bookService.addBook(bookDto));
    }

    // 3️⃣ Get Book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // 4️⃣ Get All Books (Paged)
    @GetMapping
    public ResponseEntity<Page<BookDto>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.findByDeletedFalse(pageable));
    }

    // 5️⃣ Search Books by Keyword
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword));
    }

    // 6️⃣ Get Books by Category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Book>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(bookService.findByCategoryIgnoreCase(category));
    }

    // 7️⃣ Update Book Details
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
        return ResponseEntity.ok(bookService.updateBook(id, bookDto));
    }

    // 8️⃣ Soft Delete Book
    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteBook(@PathVariable Long id) {
        bookService.softDeleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }

    // 9️⃣ Serve Book Images
    @GetMapping("/images/{filename}")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        return fileStorageService.getImage(filename);
    }
}
