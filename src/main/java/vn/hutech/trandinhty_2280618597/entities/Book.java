package vn.hutech.trandinhty_2280618597.entities;

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
    
    @Field("image")
    private String imageUrl;
}