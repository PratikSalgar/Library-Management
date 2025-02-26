package com.libraryManagement.service.Impl;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.Exception.*;
import com.libraryManagement.model.Book;
import com.libraryManagement.model.User;
import com.libraryManagement.repository.BookRepository;
import com.libraryManagement.repository.UserRepository;
import com.libraryManagement.service.BookService;
import com.libraryManagement.service.BookSpecification;
import com.libraryManagement.service.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookServiceImpl(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    private CustomUserDetail getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetail) authentication.getPrincipal();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Book addBook(BookDto bookDto) {
        CustomUserDetail loggedInUser = getLoggedInUser();
        Long loggedInUserId = loggedInUser.getUserId();

        if (!loggedInUserId.equals(bookDto.getUserId())) {
            throw new UnauthorizedException("Admin can only add books using their own ID.");
        }

        User user = userRepository.findById(bookDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + bookDto.getUserId()));

        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            throw new DatabaseException("A book with the same ISBN already exists.");
        }

        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setPublishedYear(bookDto.getPublishedYear());
        book.setCategory(bookDto.getCategory());
        book.setAvailable(true);
        book.setDeleted(false);
        book.setUser(user);

        return bookRepository.save(book);
    }


    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
    }

    @Override
    public Page<BookDto> findByDeletedFalse(Pageable pageable) {
        return bookRepository.findByDeletedFalse(pageable)
                .map(book -> new BookDto(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getCategory(),
                        book.getPublishedYear(),
                        book.isAvailable()
                ));
    }


    @Override
    public List<Book> searchBooks(String keyword) {
        Specification<Book> spec = BookSpecification.containsKeyword(keyword);
        List<Book> books = bookRepository.findAll(spec);
        if(books.isEmpty())
        {
            throw new NoResultsFoundException("No books found for the keyword: "+keyword);
        }
        return books;
    }

    @Override
    public List<Book> findByCategoryIgnoreCase(String category) {
        List<Book> byCategory = bookRepository.findByCategoryIgnoreCase(category);
        if(byCategory.isEmpty())
        {
            throw new NoResultsFoundException("No books found for the keyword: "+category);
        }
        return byCategory;
    }

    @Override
    public Book updateBook(Long id, BookDto bookDto) {
        CustomUserDetail loggedInUser = getLoggedInUser();
        Long loggedInUserId = loggedInUser.getUserId();

        Book book = getBookById(id);
        if (!book.getUser().getId().equals(loggedInUserId) && !loggedInUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("You are not authorized to update this book.");
        }

        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setPublishedYear(bookDto.getPublishedYear());
        book.setCategory(bookDto.getCategory());

        return bookRepository.save(book);
    }

    @Override
    public void softDeleteBook(Long id) {
        Book book = getBookById(id);
        if (book.isAvailable()) {
            book.setDeleted(true);
            book.setAvailable(false);
            bookRepository.save(book);
        } else {
            throw new BadRequestException("Book is already deleted");
        }
    }
}
