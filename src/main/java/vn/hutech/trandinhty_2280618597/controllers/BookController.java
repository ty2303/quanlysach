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

    @PostMapping
    public String createBook(@ModelAttribute Book book,
            @RequestParam(value = "imageFiles", required = false) org.springframework.web.multipart.MultipartFile[] imageFiles) {
        processImageUrls(book, null, imageFiles);
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    @PostMapping("/update")
    public String updateBook(@ModelAttribute Book book,
            @RequestParam(value = "keptImageUrls", required = false) List<String> keptImageUrls,
            @RequestParam(value = "imageFiles", required = false) org.springframework.web.multipart.MultipartFile[] imageFiles) {
        processImageUrls(book, keptImageUrls, imageFiles);
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    private void processImageUrls(Book book, List<String> keptImageUrls,
            org.springframework.web.multipart.MultipartFile[] imageFiles) {
        List<String> finalUrls = new java.util.ArrayList<>();

        // 1. Giữ lại các URL cũ (nếu có)
        if (keptImageUrls != null && !keptImageUrls.isEmpty()) {
            finalUrls.addAll(keptImageUrls);
        }

        // 2. Thêm các file mới upload
        if (imageFiles != null) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String fileName = fileStorageService.storeFile(file);
                        // Thêm đường dẫn file vào list (có prefix /uploads/)
                        finalUrls.add("/uploads/" + fileName);
                    } catch (Exception e) {
                        e.printStackTrace(); // Log lỗi nếu upload thất bại
                    }
                }
            }
        }

        // 3. Cập nhật vào book
        book.setImageUrls(finalUrls);
    }

    // Import Excel
    @Autowired
    private vn.hutech.trandinhty_2280618597.services.BookImportService bookImportService;

    @GetMapping("/import")
    public String showImportForm() {
        return "books/import";
    }

    @PostMapping("/import")
    public String importBooks(@RequestParam("excelFile") org.springframework.web.multipart.MultipartFile excelFile,
            Model model) {
        try {
            bookImportService.importBooks(excelFile);
            return "redirect:/admin/books";
        } catch (java.io.IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi import file: " + e.getMessage());
            return "books/import";
        }
    }

    @GetMapping("/import/sample")
    public void downloadSampleExcel(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=books_sample.xlsx");

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Books");

            // Header
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = { "Title", "Author", "Price", "Category", "LocalImagePath", "Description", "Quantity" };
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            // Sample Data
            org.apache.poi.ss.usermodel.Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("Clean Code");
            sampleRow.createCell(1).setCellValue("Robert C. Martin");
            sampleRow.createCell(2).setCellValue(500000);
            sampleRow.createCell(3).setCellValue("Technology");
            sampleRow.createCell(4).setCellValue("D:\\images\\clean_code.jpg");
            sampleRow.createCell(5).setCellValue("A Handbook of Agile Software Craftsmanship");
            sampleRow.createCell(6).setCellValue(100); // Quantity

            workbook.write(response.getOutputStream());
        }
    }

}
