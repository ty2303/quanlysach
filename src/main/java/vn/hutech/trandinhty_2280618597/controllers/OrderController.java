package vn.hutech.trandinhty_2280618597.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.hutech.trandinhty_2280618597.entities.Order;
import vn.hutech.trandinhty_2280618597.entities.User;
import vn.hutech.trandinhty_2280618597.services.OrderService;
import vn.hutech.trandinhty_2280618597.services.UserService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listOrders(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByUser(user.getId());
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable String id, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(id).orElse(null);
        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "orders/detail";
    }
}
