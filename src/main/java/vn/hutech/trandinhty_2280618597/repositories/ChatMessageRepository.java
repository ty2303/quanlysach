package vn.hutech.trandinhty_2280618597.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.hutech.trandinhty_2280618597.entities.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByUserIdOrderByTimestampAsc(String userId);
}
