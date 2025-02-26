package com.libraryManagement.repository;

import com.libraryManagement.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Page<Book> findByDeletedFalse(Pageable pageable);

    List<Book> findByCategoryIgnoreCase(String category);

    boolean existsByIsbn(String isbn);
}
