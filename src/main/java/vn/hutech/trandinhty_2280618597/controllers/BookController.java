package vn.hutech.trandinhty_2280618597.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.hutech.trandinhty_2280618597.entities.Book;
import vn.hutech.trandinhty_2280618597.services.BookService;
import vn.hutech.trandinhty_2280618597.services.CategoryService;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "books/create";
    }

    @PostMapping
    public String createBook(@ModelAttribute Book book) {
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        Book book = bookService.getBookById(id).orElse(new Book());
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "books/edit";
    }

    @PostMapping("/update")
    public String updateBook(@ModelAttribute Book book) {
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable String id) {
        bookService.deleteBookById(id);
        return "redirect:/admin/books";
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String keyword, Model model) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/list";
    }
}