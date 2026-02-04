package vn.hutech.trandinhty_2280618597.controllers;

import java.util.List;
import java.util.ArrayList;

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

    // 1. Mapping cụ thể phải đặt TRƯỚC mapping tổng quát
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "books/create";
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        Book book = bookService.getBookById(id).orElse(new Book());
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "books/edit";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") String id) {
        bookService.deleteBookById(id);
        return "redirect:/admin/books";
    }

    // 2. Mapping tổng quát đặt CUỐI
    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String showBookDetail(@PathVariable("id") String id, Model model) {
        Book book = bookService.getBookById(id).orElse(null);
        model.addAttribute("book", book);
        return "books/detail";
    }

    // POST mappings
    @Autowired
    private vn.hutech.trandinhty_2280618597.services.FileStorageService fileStorageService;

    // POST mappings
    @PostMapping
    public String createBook(@ModelAttribute Book book,
            @RequestParam(value = "imageUrls", required = false) String imageUrlsStr,
            @RequestParam(value = "imageFiles", required = false) org.springframework.web.multipart.MultipartFile[] imageFiles) {
        processImageUrls(book, imageUrlsStr, imageFiles);
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    @PostMapping("/update")
    public String updateBook(@ModelAttribute Book book,
            @RequestParam(value = "imageUrls", required = false) String imageUrlsStr,
            @RequestParam(value = "imageFiles", required = false) org.springframework.web.multipart.MultipartFile[] imageFiles) {
        processImageUrls(book, imageUrlsStr, imageFiles);
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    private void processImageUrls(Book book, String imageUrlsStr,
            org.springframework.web.multipart.MultipartFile[] imageFiles) {
        List<String> urls = new java.util.ArrayList<>();

        // 1. Giữ lại các URL cũ (từ textarea)
        if (imageUrlsStr != null && !imageUrlsStr.trim().isEmpty()) {
            String[] lines = imageUrlsStr.split("\\r?\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    urls.add(line.trim());
                }
            }
        }

        // 2. Thêm các file mới upload
        if (imageFiles != null) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String fileName = fileStorageService.storeFile(file);
                        // Thêm đường dẫn file vào list (có prefix /uploads/)
                        urls.add("/uploads/" + fileName);
                    } catch (Exception e) {
                        e.printStackTrace(); // Log lỗi nếu upload thất bại
                    }
                }
            }
        }

        // 3. Cập nhật vào book
        if (!urls.isEmpty() || imageUrlsStr != null) {
            book.setImageUrls(urls);
        }
    }
}
