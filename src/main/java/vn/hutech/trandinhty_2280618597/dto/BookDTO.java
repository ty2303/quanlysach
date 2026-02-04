package vn.hutech.trandinhty_2280618597.dto;

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
    private String imageUrl;
}
