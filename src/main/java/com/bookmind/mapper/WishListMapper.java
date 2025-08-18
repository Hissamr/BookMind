package com.bookmind.mapper;

import com.bookmind.dto.BookSummaryDto;
import com.bookmind.dto.CreateWishListRequest;
import com.bookmind.dto.UpdateWishListRequest;
import com.bookmind.dto.UserSummaryDto;
import com.bookmind.dto.WishListResponse;
import com.bookmind.dto.WishListSummaryDto;
import com.bookmind.model.Book;
import com.bookmind.model.User;
import com.bookmind.model.WishList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between WishList entities and DTOs
 */
public class WishListMapper {

    /**
     * Convert WishList entity to WishListResponse DTO
     * 
     * @param wishList the wishlist entity to convert
     * @return a WishListResponse containing the wishlist data
     */
    public static WishListResponse toWishListResponse(WishList wishList) {
        if (wishList == null) {
            return null;
        }
        
        UserSummaryDto userSummary = toUserSummary(wishList.getUser());
        List<BookSummaryDto> books = Optional.ofNullable(wishList.getBooks())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(WishListMapper::toBookSummary)
                .collect(Collectors.toUnmodifiableList());
        
        return new WishListResponse(
                wishList.getId(),
                wishList.getName(),
                userSummary,
                books,
                books.size(),
                wishList.getCreatedAt(),
                wishList.getUpdatedAt()
        );
    }
    
    /**
     * Convert WishList entity to WishListSummaryDto (without book details)
     * 
     * @param wishList the wishlist entity to convert
     * @return a summary DTO of the wishlist
     */
    public static WishListSummaryDto toWishListSummary(WishList wishList) {
        if (wishList == null) {
            return null;
        }
        
        return new WishListSummaryDto(
                wishList.getId(),
                wishList.getName(),
                Optional.ofNullable(wishList.getBooks()).map(List::size).orElse(0),
                wishList.getCreatedAt(),
                wishList.getUpdatedAt()
        );
    }
    
    /**
     * Convert User entity to UserSummaryDto
     * 
     * @param user the user entity to convert
     * @return a UserSummaryDto containing essential user information
     */
    public static UserSummaryDto toUserSummary(User user) {
        return Optional.ofNullable(user)
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail()
                ))
                .orElse(null);
    }
    
    /**
     * Convert Book entity to BookSummaryDto
     * 
     * @param book the book entity to convert
     * @return a BookSummaryDto containing essential book information
     */
    public static BookSummaryDto toBookSummary(Book book) {
        return Optional.ofNullable(book)
                .map(b -> new BookSummaryDto(
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getGenre(),
                        b.getPrice(),
                        b.getAvailable(),
                        b.getAverageRating(),
                        b.getCoverImageUrl(),
                        b.getIsbn()
                ))
                .orElse(null);
    }
    
    /**
     * Convert CreateWishListRequest to WishList entity
     * 
     * @param request the request containing wishlist creation data
     * @param user the user who owns the wishlist
     * @return a new WishList entity populated with data from the request
     * @throws IllegalArgumentException if user is null
     */
    public static WishList toWishList(CreateWishListRequest request, User user) {
        if (request == null) {
            return null;
        }
        
        Objects.requireNonNull(user, "User cannot be null when creating a wishlist");
        
        WishList wishList = new WishList();
        wishList.setName(request.getName());
        wishList.setUser(user);
        return wishList;
    }
    
    /**
     * Immutable parameters class for wishlist updates
     */
    public static final class UpdateWishListParams {
        public final Long userId;
        public final Long wishListId;
        public final String newName;
        
        private UpdateWishListParams(UpdateWishListRequest request) {
            this.userId = request.getUserId();
            this.wishListId = request.getWishListId();
            this.newName = request.getName();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UpdateWishListParams that = (UpdateWishListParams) o;
            return Objects.equals(userId, that.userId) &&
                   Objects.equals(wishListId, that.wishListId) &&
                   Objects.equals(newName, that.newName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(userId, wishListId, newName);
        }
    }
    
    /**
     * Extract parameters from UpdateWishListRequest
     * 
     * @param request the update request to extract parameters from
     * @return a parameter object containing the extracted values
     * @throws NullPointerException if request is null
     */
    public static UpdateWishListParams extractUpdateParams(UpdateWishListRequest request) {
        Objects.requireNonNull(request, "UpdateWishListRequest cannot be null");
        return new UpdateWishListParams(request);
    }
    
    /**
     * Convert list of WishList entities to list of WishListResponse DTOs
     * 
     * @param wishLists list of wishlist entities to convert
     * @return immutable list of wishlist response DTOs, empty list if input is null
     */
    public static List<WishListResponse> toWishListResponseList(List<WishList> wishLists) {
        return Optional.ofNullable(wishLists)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(WishListMapper::toWishListResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }
    
    /**
     * Convert list of WishList entities to list of WishListSummaryDto
     * 
     * @param wishLists list of wishlist entities to convert
     * @return immutable list of wishlist summary DTOs, empty list if input is null
     */
    public static List<WishListSummaryDto> toWishListSummaryList(List<WishList> wishLists) {
        return Optional.ofNullable(wishLists)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(WishListMapper::toWishListSummary)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }
}
