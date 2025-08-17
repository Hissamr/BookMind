package com.bookmind.service;

import com.bookmind.exception.BookAlreadyInWishListException;
import com.bookmind.exception.BookNotFoundException;
import com.bookmind.exception.BookNotInWishListException;
import com.bookmind.exception.WishListNotFoundException;
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
     * @throws WishListNotFoundException if the Wishlist is not found
     */
    public WishList getWishListById(Long id) {
        return wishListRepository.findById(id)
                    .orElseThrow(() -> new WishListNotFoundException(id));
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
     * @throws WishListNotFoundException if the Wishlist is not found
     */
    public WishList updateWishList(Long id, WishList wishList) {
        if(!wishListRepository.existsById(id)) {
            throw new WishListNotFoundException(id);
        }
        wishList.setId(id); //Ensure the ID is set for the update
        return wishListRepository.save(wishList);
    }

    /**
     * Delete a Wishlist by its ID.
     * @param id ID of the Wishlist to be deleted
     * @throws WishListNotFoundException if the Wishlist is not found
     */
    public void deleteWishList(Long id) {
        if(!wishListRepository.existsById(id)){
            throw new WishListNotFoundException(id);
        }
        wishListRepository.deleteById(id);
    }

    /**
     * Add a Book to a Wishlist.
     * @param bookId ID of the Book
     * @param wishListId ID of the WishList
     * @throws BookNotFoundException if the Book is not found
     * @throws WishListNotFoundException if the WishList is not found
     * @throws BookAlreadyInWishListException if the Book is already in the wishlist
     */
    public void addBookToWishList(Long bookId, Long wishListId) {
        WishList wishList = getWishListById(wishListId); 
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        
        // Check if book is already in wishlist
        if (wishList.getBooks().contains(book)) {
            throw new BookAlreadyInWishListException(bookId, wishListId);
        }
        
        wishList.addBook(book);
        wishListRepository.save(wishList);
    }

    /**
     * Remove a Book from a Wishlist.
     * @param bookId ID of the Book
     * @param wishListId ID of the WishList
     * @throws BookNotFoundException if the Book is not found
     * @throws WishListNotFoundException if the WishList is not found
     * @throws BookNotInWishListException if the Book is not in the wishlist
     */
    public void removeBookFromWishList(Long bookId, Long wishListId) {
        WishList wishList = getWishListById(wishListId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        
        // Check if book exists in wishlist before removing
        if (!wishList.getBooks().contains(book)) {
            throw new BookNotInWishListException(bookId, wishListId);
        }
        
        wishList.removeBook(book);
        wishListRepository.save(wishList);
    }

}
