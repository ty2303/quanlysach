package vn.hutech.trandinhty_2280618597.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import vn.hutech.trandinhty_2280618597.entities.Book;

public interface BookRepository extends MongoRepository<Book, String> {
    
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    @Query("{ 'author': { $regex: ?0, $options: 'i' } }")
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    List<Book> findByCategoryId(String categoryId);
}