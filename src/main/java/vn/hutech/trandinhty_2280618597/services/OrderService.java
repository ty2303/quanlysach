package vn.hutech.trandinhty_2280618597.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.entities.Cart;
import vn.hutech.trandinhty_2280618597.entities.Order;
import vn.hutech.trandinhty_2280618597.repositories.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    public Order createOrder(String userId, String username) {
        return createOrderWithPaymentMethod(userId, username, Order.PAYMENT_METHOD_COD);
    }

    public Order createOrderWithPaymentMethod(String userId, String username, String paymentMethod) {
        Cart cart = cartService.getCart(userId);

        if (cart.getItems().isEmpty()) {
            return null;
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setUsername(username);
        order.setItems(cart.getItems());
        order.setTotalAmount(cart.getTotalAmount());
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

        // Clear cart after order is created
        cartService.clearCart(userId);

        return savedOrder;
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
}
