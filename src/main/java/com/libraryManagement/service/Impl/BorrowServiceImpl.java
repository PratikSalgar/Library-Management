package com.libraryManagement.service.Impl;

import com.libraryManagement.DTO.BookDto;
import com.libraryManagement.Exception.BadRequestException;
import com.libraryManagement.Exception.ResourceNotFoundException;
import com.libraryManagement.model.Book;
import com.libraryManagement.model.BorrowLog;
import com.libraryManagement.model.User;
import com.libraryManagement.repository.BookRepository;
import com.libraryManagement.repository.BorrowLogRepository;
import com.libraryManagement.repository.UserRepository;
import com.libraryManagement.service.BorrowService;
import com.libraryManagement.service.CustomUserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BorrowServiceImpl implements BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowServiceImpl.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowLogRepository borrowLogRepository;

    public BorrowServiceImpl(BookRepository bookRepository, UserRepository userRepository, BorrowLogRepository borrowLogRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.borrowLogRepository = borrowLogRepository;
    }

    @Override
    public String borrowBooks(List<Long> bookIds, Long userId) throws ResourceNotFoundException, BadRequestException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail loggedInUser = (CustomUserDetail) authentication.getPrincipal();
        Long loggedInUserId = loggedInUser.getUserId();

        if(!loggedInUserId.equals(userId))
        {
            throw new BadRequestException("You are not authorized to borrow books on behalf of another user.");
        }

        logger.info("Attempting to borrow books: {} for User ID: {}", bookIds, userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("User with this ID {} not found", userId);
            return new ResourceNotFoundException("User not found");
        });

        List<BorrowLog> borrowLogs = borrowLogRepository.findByUserIdAndStatus(userId, BorrowLog.BorrowStatus.BORROWED);
        int currentlyBorrowed = borrowLogs.size();
        int booksToBorrow = bookIds.size();

        if (currentlyBorrowed + booksToBorrow > 3) {
            logger.error("User is trying to borrow {} books but already has {} borrowed. Limit is 3.", booksToBorrow, currentlyBorrowed);
            throw new BadRequestException("Cannot borrow more than 3 books at a time");
        }

        List<BorrowLog> borrowedBooks = borrowLogRepository.findByBookIdInAndStatus(bookIds, BorrowLog.BorrowStatus.BORROWED);
        Set<Long> borrowedBookIds = borrowedBooks.stream().map(b -> b.getBook().getId()).collect(Collectors.toSet());

        List<BorrowLog> newBorrowLogs = new ArrayList<>();
        List<Long> successfulBorrows = new ArrayList<>();
        List<Long> failedBorrows = new ArrayList<>();

        for (Long bookId : bookIds) {
            if (borrowedBookIds.contains(bookId)) {
                logger.warn("Book ID {} is already borrowed", bookId);
                failedBorrows.add(bookId);
                continue;
            }

            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) {
                logger.warn("Book ID {} not found", bookId);
                failedBorrows.add(bookId);
                continue;
            }

            if(!book.isAvailable() || book.isDeleted())
            {
                logger.warn("Book ID :{} is either unavailable or deleted", bookId );
                failedBorrows.add(bookId);
                continue;
            }

            book.setAvailable(false);
            bookRepository.save(book);

            BorrowLog borrowLog = new BorrowLog();
            borrowLog.setBook(book);
            borrowLog.setUser(user);
            borrowLog.setStatus(BorrowLog.BorrowStatus.BORROWED);
            borrowLog.setBorrowedDate(LocalDateTime.now());

            newBorrowLogs.add(borrowLog);
            successfulBorrows.add(bookId);
        }

        borrowLogRepository.saveAll(newBorrowLogs);

        String responseMessage = "Successfully borrowed books: " + successfulBorrows;
        if (!failedBorrows.isEmpty()) {
            responseMessage += ". Could not borrow books (already borrowed or not found): " + failedBorrows;
        }

        logger.info(responseMessage);
        return responseMessage;
    }


    @Override
    public String returnBooks(List<Long> bookIds, Long userId) throws ResourceNotFoundException, BadRequestException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetail loggedInUser = (CustomUserDetail) authentication.getPrincipal();
        Long loggedInUserId = loggedInUser.getUserId();

        if (!loggedInUserId.equals(userId)) {
            throw new BadRequestException("You are not authorized to return books on behalf of another user.");
        }

        logger.info("Attempting to return books: {} for User ID: {}", bookIds, userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            logger.error("User with ID {} not found", userId);
            return new ResourceNotFoundException("User not found");
        });

        List<Long> successfulReturns = new ArrayList<>();
        List<Long> failedReturns = new ArrayList<>();
        List<BorrowLog> borrowLogsToUpdate = new ArrayList<>();

        for (Long bookId : bookIds) {
            BorrowLog borrowLog = borrowLogRepository.findByUserIdAndBookIdAndStatus(userId, bookId, BorrowLog.BorrowStatus.BORROWED)
                    .orElse(null);

            if (borrowLog == null) {
                logger.warn("Book ID {} was not borrowed by User ID {}", bookId, userId);
                failedReturns.add(bookId);
                continue;
            }

            borrowLog.setStatus(BorrowLog.BorrowStatus.RETURNED);
            borrowLog.setReturnedDate(LocalDateTime.now());
            borrowLogsToUpdate.add(borrowLog);
            successfulReturns.add(bookId);


            Book book = borrowLog.getBook();
            book.setAvailable(true);
            bookRepository.save(book);
        }

        if (!borrowLogsToUpdate.isEmpty()) {
            borrowLogRepository.saveAll(borrowLogsToUpdate);
        }

        String responseMessage = "Successfully Returned books: " + successfulReturns;
        if (!failedReturns.isEmpty()) {
            responseMessage += ". Could not return books (already returned or not found): " + failedReturns;
        }
        logger.info(responseMessage);
        return responseMessage;
    }



    @Override
    public List<BookDto> getMostBorrowedBookInLast30Days() {
        logger.info("Fetching top 6 most borrowed books in the last 30 days");

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        Pageable topSix = PageRequest.of(0, 5); // we can set limit how many books we want to render
        List<Object[]> borrowCountList = borrowLogRepository.findBorrowCountForBooks(startDate, topSix);

        if (borrowCountList.isEmpty()) {
            logger.error("No borrowing records found in the last 30 days");
            throw new ResourceNotFoundException("No borrowing records found in the last 30 days");
        }

        return borrowCountList.stream()
                .map(record -> {
                    Book book = (Book) record[0];
                    long borrowCount = (long) record[1];
                    return new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedYear(), borrowCount,book.getCategory());
                })
                .collect(Collectors.toList());
    }
}