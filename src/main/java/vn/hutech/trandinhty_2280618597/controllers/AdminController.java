package vn.hutech.trandinhty_2280618597.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.hutech.trandinhty_2280618597.repositories.BookRepository;
import vn.hutech.trandinhty_2280618597.repositories.CategoryRepository;
import vn.hutech.trandinhty_2280618597.repositories.OrderRepository;
import vn.hutech.trandinhty_2280618597.repositories.UserRepository;
import vn.hutech.trandinhty_2280618597.services.BookService;
import vn.hutech.trandinhty_2280618597.services.CategoryService;
import vn.hutech.trandinhty_2280618597.services.OrderService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        // Statistics
        long totalBooks = bookRepository.count();
        long totalCategories = categoryRepository.count();
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();

        // Calculate total revenue
        double totalRevenue = orderService.getAllOrders().stream()
                .mapToDouble(order -> order.getTotalAmount())
                .sum();

        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        // Recent data
        model.addAttribute("recentBooks", bookService.getAllBooks().stream().limit(5).toList());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(5).toList());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "admin/dashboard";
    }

    @GetMapping("/books")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/books";
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }
}
