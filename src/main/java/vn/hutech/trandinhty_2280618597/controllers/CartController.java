package vn.hutech.trandinhty_2280618597.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.hutech.trandinhty_2280618597.entities.Cart;
import vn.hutech.trandinhty_2280618597.entities.Order;
import vn.hutech.trandinhty_2280618597.entities.User;
import vn.hutech.trandinhty_2280618597.services.CartService;
import vn.hutech.trandinhty_2280618597.services.OrderService;
import vn.hutech.trandinhty_2280618597.services.UserService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCart(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(user.getId());
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/add/{bookId}")
    public String addToCart(@PathVariable String bookId,
            @RequestParam(defaultValue = "1") int quantity,
            Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            cartService.addToCart(user.getId(), bookId, quantity);
        }

        return "redirect:/books";
    }

    @PostMapping("/update/{bookId}")
    public String updateQuantity(@PathVariable String bookId,
            @RequestParam int quantity,
            Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            cartService.updateQuantity(user.getId(), bookId, quantity);
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable String bookId, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            cartService.removeFromCart(user.getId(), bookId);
        }

        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(user.getId());
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderService.createOrder(user.getId(), user.getUsername());
        if (order != null) {
            return "redirect:/orders/" + order.getId();
        }

        return "redirect:/cart";
    }
}
