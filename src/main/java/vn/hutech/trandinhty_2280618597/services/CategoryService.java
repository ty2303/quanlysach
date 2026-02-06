package vn.hutech.trandinhty_2280618597.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.entities.Category;
import vn.hutech.trandinhty_2280618597.repositories.BookRepository;
import vn.hutech.trandinhty_2280618597.repositories.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Xóa danh mục và tất cả sách thuộc danh mục đó (cascade delete)
     */
    public void deleteCategoryById(String id) {
        // Xóa tất cả sách thuộc danh mục trước
        bookRepository.deleteByCategoryId(id);
        // Sau đó xóa danh mục
        categoryRepository.deleteById(id);
    }

    /**
     * Đếm số lượng sách thuộc danh mục
     */
    public long countBooksByCategory(String categoryId) {
        return bookRepository.findByCategoryId(categoryId).size();
    }
}