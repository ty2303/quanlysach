package vn.hutech.trandinhty_2280618597.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String id;
    private String title;
    private String author;
    private Double price;
    private String categoryName;
    private String description;
    private List<String> imageUrls;

    public void setImageUrl(String imageUrl) {
        // Compatibility method: does nothing or handles conversion if needed for
        // inbound DTO
    }

    public static BookDTO from(vn.hutech.trandinhty_2280618597.entities.Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPrice(book.getPrice());
        dto.setDescription(book.getDescription());
        dto.setImageUrls(book.getImageUrls());
        if (book.getCategory() != null) {
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }
}
