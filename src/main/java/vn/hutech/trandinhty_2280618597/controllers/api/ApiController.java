package vn.hutech.trandinhty_2280618597.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hutech.trandinhty_2280618597.dto.BookDTO;
import vn.hutech.trandinhty_2280618597.entities.Book;
import vn.hutech.trandinhty_2280618597.services.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {

    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> getAllBooks(
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(bookService.getAllBooks(pageNo, pageSize, sortBy)
                .stream()
                .map(BookDTO::from)
                .toList());
    }

    @GetMapping("/books/id/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        // ID in Book is String, adapting to Long if possible or changing to String path
        // variable
        // The Book entity uses String id, but prompt used Long. I will use String to
        // match Entity.
        return ResponseEntity.ok(bookService.getBookById(String.valueOf(id))
                .map(BookDTO::from)
                .orElse(null));
    }

    // Overload for String ID if needed, but the prompt specifically used Long.
    // Since Mongodb IDs are usually Strings, I will assume the prompt might be
    // generic.
    // However, I'll stick to String locally but accept Long in signature to match
    // prompt if I must,
    // but better to catch String since it's cleaner for Mongo.
    // Let's actually use String in the signature to avoid type casting issues if
    // the user passes a non-numeric ID.
    // Wait, prompt said: `public ResponseEntity<BookGetVm>
    // getBookById(@PathVariable Long id)`
    // I will change it to String id to be correct for this codebase.

    @GetMapping("/books/{id}")
    public ResponseEntity<BookDTO> getBookByIdString(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id)
                .map(BookDTO::from)
                .orElse(null));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable String id) {
        bookService.deleteBookById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword)
                .stream()
                .map(BookDTO::from)
                .toList());
    }

    @PostMapping("/books")
    public ResponseEntity<BookDTO> createBook(@RequestBody Book book) {
        return ResponseEntity.ok(BookDTO.from(bookService.saveBook(book)));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable String id, @RequestBody Book book) {
        book.setId(id);
        return ResponseEntity.ok(BookDTO.from(bookService.saveBook(book)));
    }
}
