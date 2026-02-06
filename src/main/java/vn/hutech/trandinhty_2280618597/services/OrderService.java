package vn.hutech.trandinhty_2280618597.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.entities.Book;
import vn.hutech.trandinhty_2280618597.entities.Cart;
import vn.hutech.trandinhty_2280618597.entities.CartItem;
import vn.hutech.trandinhty_2280618597.entities.Order;
import vn.hutech.trandinhty_2280618597.repositories.BookRepository;
import vn.hutech.trandinhty_2280618597.repositories.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private VoucherService voucherService;

    public Order createOrder(String userId, String username) {
        return createOrderWithPaymentMethod(userId, username, Order.PAYMENT_METHOD_COD);
    }

    public Order createOrderWithPaymentMethod(String userId, String username, String paymentMethod) {
        return createOrderWithVoucher(userId, username, paymentMethod, null);
    }

    public Order createOrderWithVoucher(String userId, String username, String paymentMethod, String voucherCode) {
        Cart cart = cartService.getCart(userId);

        if (cart.getItems().isEmpty()) {
            return null;
        }

        double originalAmount = cart.getTotalAmount();
        double discountAmount = 0;

        // Áp dụng voucher nếu có
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            String validationError = voucherService.validateVoucher(voucherCode, originalAmount);
            if (validationError == null) {
                discountAmount = voucherService.calculateDiscount(voucherCode, originalAmount);
                voucherService.applyVoucher(voucherCode);
            }
        }

        double finalAmount = originalAmount - discountAmount;

        Order order = new Order();
        order.setUserId(userId);
        order.setUsername(username);
        order.setItems(cart.getItems());
        order.setOriginalAmount(originalAmount);
        order.setDiscountAmount(discountAmount);
        order.setVoucherCode(voucherCode);
        order.setTotalAmount(finalAmount);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);

        // Set initial status based on payment method
        if (Order.PAYMENT_METHOD_MOMO.equals(paymentMethod)) {
            order.setStatus(Order.STATUS_PENDING);
            order.setPaymentStatus(Order.PAYMENT_STATUS_PENDING);
            // Generate unique request ID for MoMo
            order.setMomoRequestId(String.valueOf(System.currentTimeMillis()));
        } else {
            // COD - order is pending payment on delivery
            order.setStatus(Order.STATUS_PENDING);
            order.setPaymentStatus(Order.PAYMENT_STATUS_PENDING);
        }

        Order savedOrder = orderRepository.save(order);

        // Trừ số lượng tồn kho sau khi tạo đơn hàng thành công
        reduceInventory(cart.getItems());

        // Clear cart after order is created
        cartService.clearCart(userId);

        return savedOrder;
    }

    /**
     * Trừ số lượng tồn kho dựa trên các sản phẩm trong giỏ hàng
     */
    private void reduceInventory(List<CartItem> items) {
        for (CartItem item : items) {
            if (item.getBook() != null && item.getBook().getId() != null) {
                Optional<Book> bookOpt = bookRepository.findById(item.getBook().getId());
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    int currentQuantity = book.getQuantity() != null ? book.getQuantity() : 0;
                    int newQuantity = currentQuantity - item.getQuantity();
                    book.setQuantity(Math.max(0, newQuantity)); // Không cho phép số lượng âm
                    bookRepository.save(book);
                }
            }
        }
    }

    /**
     * Khôi phục số lượng tồn kho khi đơn hàng bị hủy
     */
    private void restoreInventory(List<CartItem> items) {
        for (CartItem item : items) {
            if (item.getBook() != null && item.getBook().getId() != null) {
                Optional<Book> bookOpt = bookRepository.findById(item.getBook().getId());
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    int currentQuantity = book.getQuantity() != null ? book.getQuantity() : 0;
                    book.setQuantity(currentQuantity + item.getQuantity());
                    bookRepository.save(book);
                }
            }
        }
    }

    public void updatePaymentStatus(String momoRequestId, String paymentStatus, String momoTransId) {
        Optional<Order> orderOpt = orderRepository.findByMomoRequestId(momoRequestId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setPaymentStatus(paymentStatus);
            order.setMomoTransId(momoTransId);
            if (Order.PAYMENT_STATUS_PAID.equals(paymentStatus)) {
                order.setStatus(Order.STATUS_CONFIRMED);
                order.setPaymentDate(LocalDateTime.now());
            } else if (Order.PAYMENT_STATUS_FAILED.equals(paymentStatus)) {
                order.setStatus(Order.STATUS_CANCELLED);
                // Khôi phục số lượng tồn kho khi thanh toán thất bại
                restoreInventory(order.getItems());
            }
            orderRepository.save(order);
        }
    }

    public Optional<Order> findByMomoRequestId(String momoRequestId) {
        return orderRepository.findByMomoRequestId(momoRequestId);
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public boolean updateOrderStatus(String orderId, String newStatus) {
        // Validate status
        if (!isValidStatus(newStatus)) {
            return false;
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            String oldStatus = order.getStatus();
            order.setStatus(newStatus);

            // Update payment status if order is completed with COD
            if (Order.STATUS_COMPLETED.equals(newStatus) &&
                    Order.PAYMENT_METHOD_COD.equals(order.getPaymentMethod())) {
                order.setPaymentStatus(Order.PAYMENT_STATUS_PAID);
                order.setPaymentDate(LocalDateTime.now());
            }

            // Khôi phục số lượng tồn kho nếu đơn hàng bị hủy
            if (Order.STATUS_CANCELLED.equals(newStatus) && !Order.STATUS_CANCELLED.equals(oldStatus)) {
                restoreInventory(order.getItems());
            }

            orderRepository.save(order);
            return true;
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        return Order.STATUS_PENDING.equals(status) ||
                Order.STATUS_CONFIRMED.equals(status) ||
                Order.STATUS_COMPLETED.equals(status) ||
                Order.STATUS_CANCELLED.equals(status);
    }
}
