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

    // Payment fields
    @Field("paymentMethod")
    private String paymentMethod;

    @Field("paymentStatus")
    private String paymentStatus;

    @Field("momoTransId")
    private String momoTransId;

    @Field("momoRequestId")
    private String momoRequestId;

    @Field("paymentDate")
    private LocalDateTime paymentDate;

    // Voucher fields
    @Field("voucherCode")
    private String voucherCode;

    @Field("discountAmount")
    private double discountAmount = 0;

    @Field("originalAmount")
    private double originalAmount; // Tổng tiền gốc trước khi giảm

    // Order status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Payment method constants
    public static final String PAYMENT_METHOD_COD = "COD";
    public static final String PAYMENT_METHOD_MOMO = "MOMO";

    // Payment status constants
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
}
