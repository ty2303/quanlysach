package vn.hutech.trandinhty_2280618597.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.hutech.trandinhty_2280618597.entities.Cart;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(String userId);

    void deleteByUserId(String userId);
}
