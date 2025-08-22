package com.bookmind.service;

import com.bookmind.exception.BookAlreadyInWishListException;
import com.bookmind.exception.BookNotFoundException;
import com.bookmind.exception.BookNotInWishListException;
import com.bookmind.exception.UserNotFoundException;
import com.bookmind.exception.WishListAlreadyExistsException;
import com.bookmind.exception.WishListNotFoundException;
import com.bookmind.mapper.WishListMapper;
import com.bookmind.model.WishList;
import com.bookmind.model.Book;
import com.bookmind.model.User;
import com.bookmind.repository.BookRepository;
import com.bookmind.repository.WishListRepository;
import com.bookmind.repository.UserRepository;
import com.bookmind.dto.SuccessResponse;
import com.bookmind.dto.WishListResponse;
import com.bookmind.dto.GetUserWishListsRequest;
import com.bookmind.dto.GetWishListRequest;
import com.bookmind.dto.CreateWishListRequest;
import com.bookmind.dto.UpdateWishListRequest;
import com.bookmind.dto.DeleteWishListRequest;
import com.bookmind.dto.AddBookToWishListRequest;
import com.bookmind.dto.BulkAddBooksToWishListRequest;
import com.bookmind.dto.BulkOperationDetail;
import com.bookmind.dto.BulkOperationResponse;
import com.bookmind.dto.BulkRemoveBookFromWishListRequest;
import com.bookmind.dto.RemoveBookFromWishListRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListService {
    
    private final WishListRepository wishListRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Get all the Wishlist of the User from the database.
     * READ-ONLY: Uses the class-level @Transactional annotation to ensure that the method is read-only.
     * 
     * @param userId ID of the User
     * @return Wishlist List
     * @throws UserNotFoundException if the User is not found
     */
    public List<WishListResponse> getAllWishListByUserId(GetUserWishListsRequest request) {
        log.debug("Fetching all wishlists for user with ID: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        List<WishList> wishLists = new ArrayList<>(user.getWishlists());

        log.debug("Found {} wishlists for user with ID: {}", wishLists.size(), request.getUserId());
        return WishListMapper.toWishListResponseList(wishLists);
    }

    /**
     * Get a Wishlist by its ID of the User from the database.
     * READ-ONLY: Uses the class-level @Transactional annotation to ensure that the method is read-only.
     * 
     * @param userId ID of the User
     * @param wishListId ID of the WishList
     * @return WishList object as WishListResponse DTO
     * @throws UserNotFoundException if the User is not found
     * @throws WishListNotFoundException if the Wishlist is not found
     */
    public WishListResponse getWishListByUserId(GetWishListRequest request) {
        log.debug("Fetching wishlist with ID: {} for user with ID: {}", request.getWishListId(), request.getUserId());

        WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));

        log.debug("Found wishlist with ID: {} for user with ID: {}", request.getWishListId(), request.getUserId());
        return WishListMapper.toWishListResponse(wishList);
    }

    /**
     * Add a new Wishlist to the User database.
     * WRITE TRANSACTION: Overrides the class-level @Transactional annotation to allow write operations with readOnly = false.
     * Uses REQUIRED propagation: joins existing transaction or creates a new one if none exists.
     * 
     * @param userId ID of the User
     * @param wishListRequest WishListRequest object to be saved
     * @return Saved Wishlist object as WishListResponse DTO
     * @throws UserNotFoundException if the User is not found
     * @throws WishListAlreadyExistsException if a Wishlist with the same name already exists
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {Exception.class}
    )
    public WishListResponse addWishListToUser(Long userId,  CreateWishListRequest wishListRequest) {
        log.info("Adding new wishlist '{}' for user with ID: {}", wishListRequest.getName(), userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
        WishList wishList = WishListMapper.toWishList(wishListRequest, user);
        boolean exists = wishListRepository.existsUserByIdAndName(userId, wishList.getName());
        if(exists) {
            throw new WishListAlreadyExistsException(wishList.getName());
        }
        user.addWishList(wishList);
        User saveUser = userRepository.save(user);
        WishList savedWishList = saveUser.getWishlists().stream()
                .filter(wl -> wl.getName().equals(wishList.getName()))
                .findFirst()
                .orElseThrow(() -> new WishListNotFoundException("Failed to retrieve saved wishlist"));

        log.info("Successfully added new wishlist with ID: {} for user with ID: {}", savedWishList.getId(), userId);
        return WishListMapper.toWishListResponse(savedWishList);
        } catch (Exception e) {
            log.error("Error adding wishlist '{}' for user with ID: {}. Transaction will be rolled back.", wishListRequest.getName(), userId, e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }
        
    }

    /**
     * Update an existing Wishlist in the database.
     * WRITE TRANSACTION: Multiple database operations that must be atomic
     * 
     * @param wishListId ID of the Wishlist to be updated
     * @param userId ID of the User who owns the Wishlist
     * @param newName New name for the Wishlist
     * @return Updated Wishlist object
     * @throws WishListNotFoundException if the Wishlist is not found
     * @throws WishListAlreadyExistsException if a Wishlist with the same name already exists
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public WishListResponse updateWishListToUser(UpdateWishListRequest request) {
        log.info("Updating wishlist with ID: {} to name '{}' for user with ID: {}", request.getWishListId(), request.getName(), request.getUserId());

        try {
            WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                        .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
            String oldName = wishList.getName();   

            boolean exists = wishListRepository.existsByUserIdAndNameExceptId(request.getUserId(), request.getName(), request.getWishListId());
            if(exists) {
                throw new WishListAlreadyExistsException(wishList.getName());
            }
            wishList.setName(request.getName());
            WishList updatedWishList = wishListRepository.save(wishList);
            
            log.info("Successfully updated wishlist with ID: {} from '{}' to '{}' for user with ID: {}", request.getWishListId(), oldName, request.getName(), request.getUserId());
            return WishListMapper.toWishListResponse(updatedWishList);
        } catch (Exception e) {
            log.error("Error updating wishlist with ID: {} for user with ID: {}. Transaction will be rolled back.", request.getWishListId(), request.getUserId(), e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }
    }

    /**
     * Delete a Wishlist by its ID.
     * WRITE TRANSACTION: Involves removing relationships and deleting entity
     * User REQUIRED propagation with higher isolation level for consistency
     * 
     * @param wishListId ID of the Wishlist to be deleted
     * @param userId ID of the User who owns the Wishlist
     * @throws UserNotFoundException if the User is not found
     * @throws WishListNotFoundException if the Wishlist is not found
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        isolation = Isolation.REPEATABLE_READ, //Higher isolation for delete operations
        rollbackFor = {Exception.class}
    )
    public SuccessResponse deleteWishList(DeleteWishListRequest request) {
        log.info("Deleting wishlist with ID: {} for user with ID: {}", request.getWishListId(), request.getUserId());

        try {
            User user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
            WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                        .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
            String wishListName = wishList.getName();
            user.removeWishList(wishList);
            userRepository.save(user);

            log.info("Successfully deleted wishlist with ID: {} and name '{}' for user with ID: {}", request.getWishListId(), wishListName, request.getUserId());
            return new SuccessResponse(true, "WishList deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting wishlist with ID: {} for user with ID: {}. Transaction will be rolled back.", request.getWishListId(), request.getUserId(), e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }    
    }

    /**
     * Add a Book to a Wishlist.
     * WRITE TRANSACTION: Modifying wishlist-book relationship
     *      
     * @param userId ID of the User who owns the Wishlist
     * @param bookId ID of the Book
     * @param wishListId ID of the WishList
     * @throws BookNotFoundException if the Book is not found
     * @throws WishListNotFoundException if the WishList is not found
     * @throws BookAlreadyInWishListException if the Book is already in the wishlist
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public SuccessResponse addBookToWishList(AddBookToWishListRequest request) {
        log.info("Adding book with ID: {} to wishlist with ID: {} for user with ID: {}", request.getBookId(), request.getWishListId(), request.getUserId());

        try {
            WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

            // Check if book is already in wishlist
            if (wishList.getBooks().contains(book)) {
                throw new BookAlreadyInWishListException(request.getBookId(), request.getWishListId());
            }
            
            wishList.addBook(book);
            wishListRepository.save(wishList);

            log.info("Successfully added book with ID: {} to wishlist with ID: {} for user with ID: {}", request.getBookId(), request.getWishListId(), request.getUserId());
            return new SuccessResponse(true, "Book added to WishList successfully");
        } catch (Exception e) {
            log.error("Error adding book with ID: {} to wishlist with ID: {} for user with ID: {}. Transaction will be rolled back.", request.getBookId(), request.getWishListId(), request.getUserId(), e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }    
    }

    /**
     * Remove a Book from a Wishlist.
     * WRITE TRANSACTION: Modifying wishlist-book relationship
     * 
     * @param userId ID of the User who owns the Wishlist
     * @param bookId ID of the Book
     * @param wishListId ID of the WishList
     * @throws BookNotFoundException if the Book is not found
     * @throws WishListNotFoundException if the WishList is not found
     * @throws BookNotInWishListException if the Book is not in the wishlist
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRED,
        rollbackFor = {Exception.class}
    )
    public SuccessResponse removeBookFromWishList(RemoveBookFromWishListRequest request) {
        log.info("Removing book with ID: {} from wishlist with ID: {} for user with ID: {}", request.getBookId(), request.getWishListId(), request.getUserId());

        try {
            WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new BookNotFoundException(request.getBookId()));

            // Check if book is already in wishlist
            if (!wishList.getBooks().contains(book)) {
                throw new BookNotInWishListException(request.getBookId(), request.getWishListId());
            }
            
            wishList.removeBook(book);
            wishListRepository.save(wishList);

            log.info("Successfully removed book with ID: {} from wishlist with ID: {} for user with ID: {}", request.getBookId(), request.getWishListId(), request.getUserId());
            return new SuccessResponse(true, "Book removed from WishList successfully");
        } catch (Exception e) {
            log.error("Error removing book with ID: {} from wishlist with ID: {} for user with ID: {}. Transaction will be rolled back.", request.getBookId(), request.getWishListId(), request.getUserId(), e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }    
    }

    /**
     * Bulk add multiple books to a Wishlist.
     * WRITE TRANSACTION: Multiple database operations that must be atomic
     * 
     * @param request BulkAddBooksToWishListRequest containing userId, wishListId, and list of bookIds
     * @throws WishListNotFoundException if the Wishlist is not found
     * @throws BookNotFoundException if any of the Books are not found
     * @throws BookAlreadyInWishListException if any of the Books are already in the wishlist
     * @throws Exception for any unexpected errors
     * @return BulkOperationResponse containing the results of the operation
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRES_NEW,
        rollbackFor = {Exception.class},
        timeout = 60 // 60 seconds timeout for bulk operations
    )
    public BulkOperationResponse addMultipleBooksToWishList(BulkAddBooksToWishListRequest request) {
        log.info("Adding {} books to wishlist with ID: {} for user with ID: {}", request.getBookIds().size(), request.getWishListId(), request.getUserId());

        List<BulkOperationDetail> details = new ArrayList<>();
        int successCount = 0, skippedCount = 0, failedCount = 0;

        try {
            WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));

            for (Long bookId : request.getBookIds()) {
                BulkOperationDetail detail = BulkOperationDetail.builder().bookId(bookId).build();
                
                try {
                    Book book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new BookNotFoundException(bookId));

                    detail.setBookDescription(book.getTitle());

                    if (wishList.getBooks().contains(book)) {
                        detail.setStatus("SKIPPED");
                        detail.setReason("Book already exists in WishList");
                        skippedCount++;
                        log.debug("Book with ID: {} already exists in wishlist with ID: {} for user with ID: {}, skipping", bookId, request.getWishListId(), request.getUserId());
                    } else {
                        wishList.addBook(book);
                        detail.setStatus("SUCCESS");
                        detail.setReason("Book added successfully");
                        successCount++;
                        log.debug("Successfully added book with ID: {} to wishlist with ID: {} for user with ID: {}", bookId, request.getWishListId(), request.getUserId());
                    }
                } catch (BookNotFoundException e) {
                    detail.setStatus("FAILED");
                    detail.setReason("Book not found");
                    detail.setBookDescription("Unknown Book"); 
                    failedCount++;
                    log.warn("Book with ID: {} not found, marking as failed", bookId);
                } catch (Exception e) {
                    detail.setStatus("FAILED");
                    detail.setReason("Unexpected error: " + e.getMessage());
                    failedCount++;
                    log.warn("Unexpected error occurred while processing book with ID: {}", bookId, e);
                }
                details.add(detail);
            }

            if (successCount > 0) {
                wishListRepository.save(wishList);
                log.info("Successfully added {} books to wishlist with ID: {} for user with ID: {}", successCount, request.getWishListId(), request.getUserId());
            }

            BulkOperationResponse response = BulkOperationResponse.builder()
                    .success(successCount > 0 || (skippedCount > 0 && failedCount == 0))
                    .message(String.format("Processed %d books: %d added, %d skipped, %d failed", 
                            request.getBookIds().size(), successCount, skippedCount, failedCount))
                    .totalRequested(request.getBookIds().size())
                    .successfullyProcessed(successCount)
                    .skipped(skippedCount)
                    .failed(failedCount)
                    .details(details)
                    .build();

            log.info("Bulk add operation completed: {} out of {} books processed successfully", 
                    successCount, request.getBookIds().size());
                    
             return response;       
        } catch (WishListNotFoundException e) {
            log.error("WishList not found for bulk add operation: {}", e.getMessage());
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        } catch (Exception e) {
            log.error("Error during bulk add operation for wishlist with ID: {} for user with ID: {}. Transaction will be rolled back.", request.getWishListId(), request.getUserId(), e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }
    }

    /**
     * Bulk remove multiple books to a Wishlist.
     * WRITE TRANSACTION: Multiple database operations that must be atomic
     * 
     * @param request BulkRemoveBooksToWishListRequest containing userId, wishListId, and list of bookIds
     * @throws WishListNotFoundException if the Wishlist is not found
     * @throws BookNotFoundException if any of the Books are not found
     * @throws BookAlreadyInWishListException if any of the Books are already in the wishlist
     * @throws Exception for any unexpected errors
     * @return BulkOperationResponse containing the results of the operation
     */
    @Transactional(
        readOnly = false,
        propagation = Propagation.REQUIRES_NEW,
        rollbackFor = {Exception.class},
        timeout = 60 // 60 seconds timeout for bulk operations
    )
    public BulkOperationResponse removeMultipleBooksFromWishList(BulkRemoveBookFromWishListRequest request) {
        log.info("Removing {} books from wishlist with ID: {} for user with ID: {}", request.getBookIds().size(), request.getWishListId(), request.getUserId());

        List<BulkOperationDetail> details = new ArrayList<>();
        int successCount = 0, skippedCount = 0, failedCount = 0;

        try {
            WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
            
            for(Long bookId : request.getBookIds()) {
                BulkOperationDetail detail = BulkOperationDetail.builder().bookId(bookId).build();

                try {
                    Book book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new BookNotFoundException(bookId));

                    detail.setBookDescription(book.getTitle());

                    if(!wishList.getBooks().contains(book)) {
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
                } catch (Exception e) {
                    detail.setStatus("FAILED");
                    detail.setReason("Unexpected error: " + e.getMessage());
                    failedCount++;
                }
                details.add(detail);
            }

            if (successCount > 0) {
                wishListRepository.save(wishList);
            }

            BulkOperationResponse response = BulkOperationResponse.builder()
                    .success(successCount > 0 || (skippedCount > 0 && failedCount == 0))
                    .message(String.format("Processed %d books: %d removed, %d skipped, %d failed", 
                            request.getBookIds().size(), successCount, skippedCount, failedCount))
                    .totalRequested(request.getBookIds().size())
                    .successfullyProcessed(successCount)
                    .skipped(skippedCount)
                    .failed(failedCount)
                    .details(details)
                    .build();

            log.info("Bulk remove operation completed: {} out of {} books processed successfully", 
                    successCount, request.getBookIds().size());

            return response;
        } catch (WishListNotFoundException e) {
            log.error("WishList not found for bulk remove operation: {}", e.getMessage());
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        } catch (Exception e) {
            log.error("Error during bulk remove operation for wishlist with ID: {} for user with ID: {}. Transaction will be rolled back.", request.getWishListId(), request.getUserId(), e);
            throw e; // Let the transaction manager handle the rollback (Re-throw to trigger rollback)
        }
    }

}
