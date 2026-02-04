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
        order.setStatus(Order.STATUS_COMPLETED);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after order
        cartService.clearCart(userId);

        return savedOrder;
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
