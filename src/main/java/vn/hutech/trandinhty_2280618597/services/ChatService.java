package vn.hutech.trandinhty_2280618597.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import vn.hutech.trandinhty_2280618597.entities.ChatMessage;

@Service
public class ChatService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private vn.hutech.trandinhty_2280618597.repositories.ChatMessageRepository chatMessageRepository;

    public String chat(String userMessage, String userId) {
        // Save User Message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setTimestamp(java.time.LocalDateTime.now());
        chatMessageRepository.save(userMsg);

        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct Gemini Request Body
        Map<String, Object> part = new HashMap<>();
        part.put("text", "Bạn là một chuyên gia tư vấn sách. Hãy trả lời ngắn gọn và nhiệt tình: " + userMessage);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    if (contentMap != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String aiResponse = (String) parts.get(0).get("text");

                            // Save AI Response
                            ChatMessage aiMsg = new ChatMessage();
                            aiMsg.setUserId(userId);
                            aiMsg.setRole("model");
                            aiMsg.setContent(aiResponse);
                            aiMsg.setTimestamp(java.time.LocalDateTime.now());
                            chatMessageRepository.save(aiMsg);

                            return aiResponse;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, tôi đang gặp lỗi kết nối với Gemini AI. (" + e.getMessage() + ")";
        }
        return "Xin lỗi, tôi không thể trả lời lúc này.";
    }

    public List<ChatMessage> getHistory(String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
    }
}
