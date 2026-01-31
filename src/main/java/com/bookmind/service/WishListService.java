package com.bookmind.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bookmind.dto.BulkOperationDetail;
import com.bookmind.dto.BulkOperationResponse;
import com.bookmind.dto.SuccessResponse;
import com.bookmind.dto.WishListResponse;
import com.bookmind.exception.BookAlreadyInWishListException;
import com.bookmind.exception.BookNotFoundException;
import com.bookmind.exception.BookNotInWishListException;
import com.bookmind.exception.UserNotFoundException;
import com.bookmind.exception.WishListAlreadyExistsException;
import com.bookmind.exception.WishListNotFoundException;
import com.bookmind.mapper.WishListMapper;
import com.bookmind.model.Book;
import com.bookmind.model.User;
import com.bookmind.model.WishList;
import com.bookmind.repository.BookRepository;
import com.bookmind.repository.UserRepository;
import com.bookmind.repository.WishListRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListService {

    private final WishListRepository wishListRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Get all wishlists for a user.
     * 
     * @param userId the authenticated user's ID
     * @return List of WishListResponse DTOs
     * @throws UserNotFoundException if the user is not found
     */
    public List<WishListResponse> getAllWishListsByUserId(Long userId) {
        log.debug("Fetching all wishlists for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<WishList> wishLists = new ArrayList<>(user.getWishlists());

        log.debug("Found {} wishlists for user with ID: {}", wishLists.size(), userId);
        return WishListMapper.toWishListResponseList(wishLists);
    }

    /**
     * Get a specific wishlist by ID.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID
     * @return WishListResponse DTO
     * @throws WishListNotFoundException if the wishlist is not found
     */
    public WishListResponse getWishListById(Long userId, Long wishListId) {
        log.debug("Fetching wishlist with ID: {} for user with ID: {}", wishListId, userId);

        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));

        log.debug("Found wishlist with ID: {} for user with ID: {}", wishListId, userId);
        return WishListMapper.toWishListResponse(wishList);
    }

    /**
     * Create a new wishlist for a user.
     * 
     * @param userId the authenticated user's ID
     * @param name the wishlist name
     * @return WishListResponse DTO
     * @throws UserNotFoundException if the user is not found
     * @throws WishListAlreadyExistsException if a wishlist with the same name exists
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {Exception.class}
    )
    public WishListResponse createWishList(Long userId, String name) {
        log.info("Creating new wishlist '{}' for user with ID: {}", name, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean exists = wishListRepository.existsUserByIdAndName(userId, name);
        if (exists) {
            throw new WishListAlreadyExistsException(name);
        }

        WishList wishList = new WishList();
        wishList.setName(name);
        wishList.setUser(user);

        user.addWishList(wishList);
        User savedUser = userRepository.save(user);

        WishList savedWishList = savedUser.getWishlists().stream()
                .filter(wl -> wl.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new WishListNotFoundException("Failed to retrieve saved wishlist"));

        log.info("Successfully created wishlist with ID: {} for user with ID: {}", savedWishList.getId(), userId);
        return WishListMapper.toWishListResponse(savedWishList);
    }

    /**
     * Update a wishlist name.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID
     * @param newName the new name
     * @return WishListResponse DTO
     * @throws WishListNotFoundException if the wishlist is not found
     * @throws WishListAlreadyExistsException if a wishlist with the same name exists
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public WishListResponse updateWishList(Long userId, Long wishListId, String newName) {
        log.info("Updating wishlist with ID: {} to name '{}' for user with ID: {}", wishListId, newName, userId);

        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));
        String oldName = wishList.getName();

        boolean exists = wishListRepository.existsByUserIdAndNameExceptId(userId, newName, wishListId);
        if (exists) {
            throw new WishListAlreadyExistsException(newName);
        }

        wishList.setName(newName);
        WishList updatedWishList = wishListRepository.save(wishList);

        log.info("Successfully updated wishlist with ID: {} from '{}' to '{}' for user with ID: {}",
                wishListId, oldName, newName, userId);
        return WishListMapper.toWishListResponse(updatedWishList);
    }

    /**
     * Delete a wishlist.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID to delete
     * @return SuccessResponse
     * @throws UserNotFoundException if the user is not found
     * @throws WishListNotFoundException if the wishlist is not found
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = {Exception.class}
    )
    public SuccessResponse deleteWishList(Long userId, Long wishListId) {
        log.info("Deleting wishlist with ID: {} for user with ID: {}", wishListId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));

        String wishListName = wishList.getName();
        user.removeWishList(wishList);
        userRepository.save(user);

        log.info("Successfully deleted wishlist with ID: {} and name '{}' for user with ID: {}",
                wishListId, wishListName, userId);
        return new SuccessResponse(true, "WishList deleted successfully");
    }

    /**
     * Add a book to a wishlist.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID
     * @param bookId the book ID to add
     * @return SuccessResponse
     * @throws WishListNotFoundException if the wishlist is not found
     * @throws BookNotFoundException if the book is not found
     * @throws BookAlreadyInWishListException if the book is already in the wishlist
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public SuccessResponse addBookToWishList(Long userId, Long wishListId, Long bookId) {
        log.info("Adding book with ID: {} to wishlist with ID: {} for user with ID: {}", bookId, wishListId, userId);

        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (wishList.getBooks().contains(book)) {
            throw new BookAlreadyInWishListException(bookId, wishListId);
        }

        wishList.addBook(book);
        wishListRepository.save(wishList);

        log.info("Successfully added book with ID: {} to wishlist with ID: {} for user with ID: {}",
                bookId, wishListId, userId);
        return new SuccessResponse(true, "Book added to WishList successfully");
    }

    /**
     * Remove a book from a wishlist.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID
     * @param bookId the book ID to remove
     * @return SuccessResponse
     * @throws WishListNotFoundException if the wishlist is not found
     * @throws BookNotFoundException if the book is not found
     * @throws BookNotInWishListException if the book is not in the wishlist
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public SuccessResponse removeBookFromWishList(Long userId, Long wishListId, Long bookId) {
        log.info("Removing book with ID: {} from wishlist with ID: {} for user with ID: {}", bookId, wishListId, userId);

        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!wishList.getBooks().contains(book)) {
            throw new BookNotInWishListException(bookId, wishListId);
        }

        wishList.removeBook(book);
        wishListRepository.save(wishList);

        log.info("Successfully removed book with ID: {} from wishlist with ID: {} for user with ID: {}",
                bookId, wishListId, userId);
        return new SuccessResponse(true, "Book removed from WishList successfully");
    }

    /**
     * Bulk add multiple books to a wishlist.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID
     * @param bookIds list of book IDs to add
     * @return BulkOperationResponse with results
     * @throws WishListNotFoundException if the wishlist is not found
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRES_NEW,
        rollbackFor = {Exception.class},
        timeout = 60
    )
    public BulkOperationResponse addMultipleBooksToWishList(Long userId, Long wishListId, List<Long> bookIds) {
        log.info("Adding {} books to wishlist with ID: {} for user with ID: {}", bookIds.size(), wishListId, userId);

        List<BulkOperationDetail> details = new ArrayList<>();
        int successCount = 0, skippedCount = 0, failedCount = 0;

        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));

        for (Long bookId : bookIds) {
            BulkOperationDetail detail = BulkOperationDetail.builder().bookId(bookId).build();

            try {
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new BookNotFoundException(bookId));

                detail.setBookDescription(book.getTitle());

                if (wishList.getBooks().contains(book)) {
                    detail.setStatus("SKIPPED");
                    detail.setReason("Book already exists in WishList");
                    skippedCount++;
                } else {
                    wishList.addBook(book);
                    detail.setStatus("SUCCESS");
                    detail.setReason("Book added successfully");
                    successCount++;
                }
            } catch (BookNotFoundException e) {
                detail.setStatus("FAILED");
                detail.setReason("Book not found");
                detail.setBookDescription("Unknown Book");
                failedCount++;
            } catch (RuntimeException e) {
                detail.setStatus("FAILED");
                detail.setReason("Unexpected error: " + e.getMessage());
                failedCount++;
            }
            details.add(detail);
        }

        if (successCount > 0) {
            wishListRepository.save(wishList);
            log.info("Successfully added {} books to wishlist with ID: {} for user with ID: {}",
                    successCount, wishListId, userId);
        }

        return BulkOperationResponse.builder()
                .success(successCount > 0 || (skippedCount > 0 && failedCount == 0))
                .message(String.format("Processed %d books: %d added, %d skipped, %d failed",
                        bookIds.size(), successCount, skippedCount, failedCount))
                .totalRequested(bookIds.size())
                .successfullyProcessed(successCount)
                .skipped(skippedCount)
                .failed(failedCount)
                .details(details)
                .build();
    }

    /**
     * Bulk remove multiple books from a wishlist.
     * 
     * @param userId the authenticated user's ID
     * @param wishListId the wishlist ID
     * @param bookIds list of book IDs to remove
     * @return BulkOperationResponse with results
     * @throws WishListNotFoundException if the wishlist is not found
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRES_NEW,
        rollbackFor = {Exception.class},
        timeout = 60
    )
    public BulkOperationResponse removeMultipleBooksFromWishList(Long userId, Long wishListId, List<Long> bookIds) {
        log.info("Removing {} books from wishlist with ID: {} for user with ID: {}", bookIds.size(), wishListId, userId);

        List<BulkOperationDetail> details = new ArrayList<>();
        int successCount = 0, skippedCount = 0, failedCount = 0;

        WishList wishList = wishListRepository.findByUserIdAndWishListId(userId, wishListId)
                .orElseThrow(() -> new WishListNotFoundException(wishListId));

        for (Long bookId : bookIds) {
            BulkOperationDetail detail = BulkOperationDetail.builder().bookId(bookId).build();

            try {
                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new BookNotFoundException(bookId));

                detail.setBookDescription(book.getTitle());

                if (!wishList.getBooks().contains(book)) {
                    detail.setStatus("SKIPPED");
                    detail.setReason("Book not in wishlist");
                    skippedCount++;
                } else {
                    wishList.removeBook(book);
                    detail.setStatus("SUCCESS");
                    detail.setReason("Book removed successfully");
                    successCount++;
                }
            } catch (BookNotFoundException e) {
                detail.setStatus("FAILED");
                detail.setReason("Book not found");
                detail.setBookDescription("Unknown book");
                failedCount++;
            } catch (RuntimeException e) {
                detail.setStatus("FAILED");
                detail.setReason("Unexpected error: " + e.getMessage());
                failedCount++;
            }
            details.add(detail);
        }

        if (successCount > 0) {
            wishListRepository.save(wishList);
        }

        return BulkOperationResponse.builder()
                .success(successCount > 0 || (skippedCount > 0 && failedCount == 0))
                .message(String.format("Processed %d books: %d removed, %d skipped, %d failed",
                        bookIds.size(), successCount, skippedCount, failedCount))
                .totalRequested(bookIds.size())
                .successfullyProcessed(successCount)
                .skipped(skippedCount)
                .failed(failedCount)
                .details(details)
                .build();
    }
}
