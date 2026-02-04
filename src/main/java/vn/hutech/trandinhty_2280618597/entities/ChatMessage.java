package vn.hutech.trandinhty_2280618597.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;

    private String userId; // User ID or Username
    private String role; // "user" or "model" matches Gemini roles, or "assistant"
    private String content;
    private LocalDateTime timestamp;
}
