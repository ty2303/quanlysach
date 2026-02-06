package vn.hutech.trandinhty_2280618597.entities;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {
    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("author")
    private String author;

    @Field("price")
    private Double price;

    @Field("category")
    private Category category;

    @Field("description")
    private String description;

    @Field("quantity")
    private Integer quantity = 0;

    @Field("images")
    private List<String> imageUrls = new ArrayList<>();

    @Field("image")
    private String legacyImageUrl;

    public List<String> getImageUrls() {
        // Migration logic: if imageUrls is empty but legacyImageUrl exists, use it
        if ((imageUrls == null || imageUrls.isEmpty()) && legacyImageUrl != null && !legacyImageUrl.isEmpty()) {
            if (imageUrls == null)
                imageUrls = new ArrayList<>();
            imageUrls.add(legacyImageUrl);
        }
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        // Sync legacy field for backward compatibility if needed, or just clear it upon
        // save?
        // Better to keep it primarily read-only or rely on list.
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.legacyImageUrl = imageUrls.get(0);
        }
    }

    // Backward compatibility for templates still using imageUrl
    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return legacyImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.legacyImageUrl = imageUrl;
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Avoid adding duplicates if it's the same as legacy
            if (!this.imageUrls.contains(imageUrl)) {
                if (this.imageUrls.isEmpty()) {
                    this.imageUrls.add(imageUrl);
                } else {
                    this.imageUrls.set(0, imageUrl);
                }
            }
        }
    }
}