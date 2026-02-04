package vn.hutech.trandinhty_2280618597.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Book book;
    private int quantity;

    public double getSubtotal() {
        return book.getPrice() * quantity;
    }
}
