package com.libraryManagement.service;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.Exception.BadRequestException;
import com.libraryManagement.Exception.ResourceNotFoundException;

import java.util.List;

public interface BorrowService {

    String borrowBooks(List<Long> bookIds, Long userId) throws ResourceNotFoundException, BadRequestException;

    String returnBooks(List<Long> bookIds, Long userId) throws ResourceNotFoundException, BadRequestException;

    List<BookDto> getMostBorrowedBookInLast30Days();
}
