package vn.hutech.trandinhty_2280618597.services;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.entities.Book;
import vn.hutech.trandinhty_2280618597.entities.Cart;
import vn.hutech.trandinhty_2280618597.entities.CartItem;
import vn.hutech.trandinhty_2280618597.repositories.CartRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookService bookService;

    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(String userId, String bookId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Optional<Book> bookOpt = bookService.getBookById(bookId);

        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();

            // Check if book already in cart
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getBook().getId().equals(bookId))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            } else {
                cart.getItems().add(new CartItem(book, quantity));
            }

            return cartRepository.save(cart);
        }

        return cart;
    }

    public Cart updateQuantity(String userId, String bookId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        // Remove item if quantity is 0 or less
        cart.getItems().removeIf(item -> item.getQuantity() <= 0);

        return cartRepository.save(cart);
    }

    public Cart removeFromCart(String userId, String bookId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(item -> item.getBook().getId().equals(bookId));
        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.setItems(new ArrayList<>());
        cartRepository.save(cart);
    }

    public Cart getCart(String userId) {
        return getOrCreateCart(userId);
    }
}
