package com.libraryManagement.controller;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.Exception.BadRequestException;
import com.libraryManagement.Exception.ResourceNotFoundException;
import com.libraryManagement.service.BorrowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BorrowController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<String> borrowBooks(@RequestParam List<Long> bookIds, @RequestParam Long userId) {
        logger.info("Received borrow request: userId={}, bookIds={}", userId, bookIds);

        if (bookIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Book IDs cannot be empty");
        }

        try {
            String response = borrowService.borrowBooks(bookIds, userId);
            logger.info("Books borrowed successfully: userId={}, bookIds={}", userId, bookIds);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BadRequestException ex) {
            logger.error("Error during borrow request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


    @PostMapping("/return")
    public ResponseEntity<String> returnBooks(@RequestParam List<Long> bookIds, @RequestParam Long userId) {
        logger.info("Received return request: userId={}, bookIds={}", userId, bookIds);

        if (bookIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Book IDs cannot be empty");
        }

        try {
            String response = borrowService.returnBooks(bookIds, userId);
            logger.info("Books returned successfully: userId={}, bookIds={}", userId, bookIds);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | BadRequestException ex) {
            logger.error("Error during return request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/most-borrowed")
    public ResponseEntity<?> getMostBorrowedBook() {
        logger.info("Received request to fetch most borrowed books in the last 30 days");

        try {
            List<BookDto> books = borrowService.getMostBorrowedBookInLast30Days();
            if (books.isEmpty()) {
                logger.info("No books borrowed in the last 30 days.");
                return ResponseEntity.ok(new ArrayList<>()); // Return an empty list instead of a message.
            }
            logger.info("Most borrowed books: {}", books.stream().map(BookDto::getTitle).collect(Collectors.joining(", ")));
            return ResponseEntity.ok(books);
        } catch (ResourceNotFoundException ex) {
            logger.error("Error fetching most borrowed books: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }


}