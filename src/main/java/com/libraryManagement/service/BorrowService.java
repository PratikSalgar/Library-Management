package com.libraryManagement.service;

import com.libraryManagement.DTO.BookDto;

import java.util.List;

public interface BorrowService {
    String borrowBooks(List<Long> bookIds, Long userId);
    String returnBooks(List<Long> bookIds, Long userId);

    List<BookDto> getMostBorrowedBookInLast30Days();
}

