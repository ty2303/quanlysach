package vn.hutech.trandinhty_2280618597.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vouchers")
public class Voucher {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("code")
    private String code;

    @Field("description")
    private String description;

    @Field("discountType")
    private String discountType; // PERCENT or FIXED

    @Field("discountValue")
    private double discountValue;

    @Field("minOrderAmount")
    private double minOrderAmount; // Đơn hàng tối thiểu

    @Field("maxDiscount")
    private Double maxDiscount; // Giảm tối đa (cho loại PERCENT)

    @Field("usageLimit")
    private Integer usageLimit; // Số lần sử dụng tối đa (null = không giới hạn)

    @Field("usedCount")
    private int usedCount = 0;

    @Field("startDate")
    private LocalDateTime startDate;

    @Field("endDate")
    private LocalDateTime endDate;

    @Field("active")
    private boolean active = true;

    @Field("createdAt")
    private LocalDateTime createdAt;

    // Discount type constants
    public static final String TYPE_PERCENT = "PERCENT";
    public static final String TYPE_FIXED = "FIXED";

    /**
     * Kiểm tra voucher có hợp lệ để sử dụng không
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra trạng thái active
        if (!active) {
            return false;
        }

        // Kiểm tra ngày bắt đầu
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }

        // Kiểm tra ngày kết thúc
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }

        // Kiểm tra số lượng sử dụng
        if (usageLimit != null && usedCount >= usageLimit) {
            return false;
        }

        return true;
    }

    /**
     * Tính số tiền giảm dựa trên tổng đơn hàng
     */
    public double calculateDiscount(double orderAmount) {
        if (!isValid() || orderAmount < minOrderAmount) {
            return 0;
        }

        double discount;
        if (TYPE_PERCENT.equals(discountType)) {
            discount = orderAmount * (discountValue / 100);
            // Áp dụng giới hạn giảm tối đa
            if (maxDiscount != null && discount > maxDiscount) {
                discount = maxDiscount;
            }
        } else {
            // FIXED
            discount = discountValue;
        }

        // Không được giảm quá tổng đơn hàng
        return Math.min(discount, orderAmount);
    }
}
