package vn.hutech.trandinhty_2280618597.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.hutech.trandinhty_2280618597.entities.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByOrderDateDesc(String userId);

    List<Order> findAllByOrderByOrderDateDesc();

    java.util.Optional<Order> findByMomoRequestId(String momoRequestId);
}
