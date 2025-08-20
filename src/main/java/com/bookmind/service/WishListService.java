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
import com.bookmind.dto.RemoveBookFromWishListRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import java.util.*;

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
        User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        List<WishList> wishLists = new ArrayList<>(user.getWishlists());

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
        WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
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
        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
        WishList wishList = WishListMapper.toWishList(wishListRequest, user);
        boolean exists = wishListRepository.existsUserByIdAndName(userId, wishList.getName());
        if(exists) {
            throw new WishListAlreadyExistsException(wishList.getName());
        }
        user.addWishList(wishList);
        userRepository.save(user);
        return WishListMapper.toWishListResponse(wishList);
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
        WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
        boolean exists = wishListRepository.existsByUserIdAndNameExceptId(request.getUserId(), request.getName(), request.getWishListId());
        if(exists) {
            throw new WishListAlreadyExistsException(wishList.getName());
        }
        wishList.setName(request.getName());
        wishListRepository.save(wishList);
        return WishListMapper.toWishListResponse(wishList);
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
        User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        WishList wishList = wishListRepository.findByUserIdAndWishListId(request.getUserId(), request.getWishListId())
                    .orElseThrow(() -> new WishListNotFoundException(request.getWishListId()));
        user.removeWishList(wishList);
        userRepository.save(user);
        return new SuccessResponse(true, "WishList deleted successfully");
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
        return new SuccessResponse(true, "Book added to WishList successfully");
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
        return new SuccessResponse(true, "Book removed from WishList successfully");
    }

}
