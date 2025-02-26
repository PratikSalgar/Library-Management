package com.libraryManagement.repository;

import com.libraryManagement.model.BorrowLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowLogRepository extends JpaRepository<BorrowLog, Long> {
    List<BorrowLog> findByUserIdAndStatus(Long userId, BorrowLog.BorrowStatus status);

    List<BorrowLog> findByBookIdInAndStatus(List<Long> bookIds, BorrowLog.BorrowStatus status);

    @Query("SELECT b FROM BorrowLog b WHERE b.user.id = :userId AND b.book.id = :bookId AND b.status = :status")
    Optional<BorrowLog> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowLog.BorrowStatus status);


    @Query("SELECT b.book, COUNT(b.book) FROM BorrowLog b WHERE b.borrowedDate >= :startDate " +
            "GROUP BY b.book ORDER BY COUNT(b.book) DESC")
    List<Object[]> findBorrowCountForBooks(LocalDateTime startDate, Pageable pageable);

}
