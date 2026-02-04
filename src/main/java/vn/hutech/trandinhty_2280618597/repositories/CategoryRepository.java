package vn.hutech.trandinhty_2280618597.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.hutech.trandinhty_2280618597.entities.Category;

public interface CategoryRepository extends MongoRepository<Category, String> {
}