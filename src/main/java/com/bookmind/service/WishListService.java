package com.bookmind.service;

import com.bookmind.model.WishList;
import com.bookmind.model.Book;
import com.bookmind.repository.BookRepository;
import com.bookmind.repository.WishListRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WishListService {
    
    private final WishListRepository wishListRepository;
    private final BookRepository bookRepository;

    /**
     * Get all the Wishlist from the database.
     * @return
     */
    public List<WishList> getAllWishList() {
        return wishListRepository.findAll();
    }

    /**
     * Get a Wishlist by its ID.
     * @param id ID of the Wishlist
     * @return WishList object
     * @throws RuntimeException if the Wishlist is not found
     */
    public WishList getWishListById(Long id) {
        return wishListRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("WishList not found with ID: " + id));
    }

    /**
     * Save a new Wishlist to the database.
     * @param wishList WishList object to be saved
     * @return Saved Wishlist object
     */
    public WishList addWishList(WishList wishList) {
        return wishListRepository.save(wishList);
    }

    /**
     * Update an existing Wishlist in the database.
     * @param id ID of the Wishlist to be updated
     * @param wishList Updated Wishlist object
     * @return Updated Wishlist object
     * @throws RuntimeException if the Wishlist is not found
     */
    public WishList updateWishList(Long id, WishList wishList) {
        if(!wishListRepository.existsById(id)) {
            throw new RuntimeException("WishList not found with ID: " + id);
        }
        wishList.setId(id); //Ensure the ID is set for the update
        return wishListRepository.save(wishList);
    }

    /**
     * Delete a Wishlist by its ID.
     * @param id ID of the Book to be deleted
     * @throws RuntimeException if the Wishlist is not found
     */
    public void deleteWishList(Long id) {
        if(!wishListRepository.existsById(id)){
            throw new RuntimeException("WishList not found with ID: " + id);
        }
        wishListRepository.deleteById(id);
    }

    /**
     * Add a Book to a Wishlist.
     * @param bookId ID of the Book
     * @param wishListId ID of the Category
     * @throws RuntimeException if the Book or WishList is not found
     */
    public void addBookToWishList(Long bookId, Long wishListId) {
        WishList wishList = getWishListById(wishListId); 
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
        wishList.addBook(book);
        wishListRepository.save(wishList);
    }

    /**
     * Remove a Book from a Wishlist.
     * @param bookId ID of the Book
     * @param wishListId ID of the WishList
     * @throws RuntimeException if the Book or WishList is not found
     */
    public void removeBookFromWishList(Long bookId, Long wishListId) {
        WishList wishList = getWishListById(wishListId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
        wishList.removeBook(book);
        wishListRepository.save(wishList);
    }

}
