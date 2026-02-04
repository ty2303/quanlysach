package vn.hutech.trandinhty_2280618597.entities;

import java.time.LocalDateTime;
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
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @Field("userId")
    private String userId;

    @Field("username")
    private String username;

    @Field("items")
    private List<CartItem> items = new ArrayList<>();

    @Field("totalAmount")
    private double totalAmount;

    @Field("orderDate")
    private LocalDateTime orderDate;

    @Field("status")
    private String status;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}
