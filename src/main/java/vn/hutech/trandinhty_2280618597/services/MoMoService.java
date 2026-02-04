package vn.hutech.trandinhty_2280618597.services;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import vn.hutech.trandinhty_2280618597.config.MoMoConfig;
import vn.hutech.trandinhty_2280618597.entities.Order;
import vn.hutech.trandinhty_2280618597.entities.momo.MoMoRequest;
import vn.hutech.trandinhty_2280618597.entities.momo.MoMoResponse;
import vn.hutech.trandinhty_2280618597.repositories.OrderRepository;

@Service
public class MoMoService {

    @Autowired
    private MoMoConfig moMoConfig;

    @Autowired
    private OrderRepository orderRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Create a MoMo payment request and return the payment URL
     */
    public String createPaymentRequest(String orderId, Long amount, String orderInfo, String requestId) {
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                moMoConfig.getAccessKey(),
                amount,
                "", // extraData
                moMoConfig.getIpnUrl(),
                orderId,
                orderInfo,
                moMoConfig.getPartnerCode(),
                moMoConfig.getRedirectUrl(),
                requestId,
                "captureWallet" // requestType for QR payment
        );

        String signature = generateSignature(rawSignature);

        MoMoRequest request = MoMoRequest.builder()
                .partnerCode(moMoConfig.getPartnerCode())
                .accessKey(moMoConfig.getAccessKey())
                .requestId(requestId)
                .amount(String.valueOf(amount))
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(moMoConfig.getRedirectUrl())
                .ipnUrl(moMoConfig.getIpnUrl())
                .extraData("")
                .requestType("captureWallet")
                .signature(signature)
                .lang("vi")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MoMoRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MoMoResponse> response = restTemplate.postForEntity(
                    moMoConfig.getApiUrl(),
                    entity,
                    MoMoResponse.class);

            MoMoResponse moMoResponse = response.getBody();
            if (moMoResponse != null && moMoResponse.getResultCode() != null && moMoResponse.getResultCode() == 0) {
                return moMoResponse.getPayUrl();
            } else {
                String errorMsg = moMoResponse != null ? moMoResponse.getMessage() : "Unknown error";
                throw new RuntimeException("MoMo payment request failed: " + errorMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MoMo payment request: " + e.getMessage(), e);
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    public String generateSignature(String rawData) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    moMoConfig.getSecretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature: " + e.getMessage(), e);
        }
    }

    /**
     * Verify callback signature from MoMo
     */
    public boolean verifySignature(String receivedSignature, String rawData) {
        String computedSignature = generateSignature(rawData);
        return computedSignature.equals(receivedSignature);
    }

    /**
     * Build raw signature string from callback parameters for verification
     */
    public String buildCallbackRawSignature(String accessKey, String amount, String extraData,
            String message, String orderId, String orderInfo, String orderType,
            String partnerCode, String payType, String requestId, String responseTime,
            String resultCode, String transId) {
        return String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                accessKey, amount, extraData, message, orderId, orderInfo, orderType,
                partnerCode, payType, requestId, responseTime, resultCode, transId);
    }

    /**
     * Handle successful payment
     */
    public void handlePaymentSuccess(String momoRequestId, String transId) {
        Optional<Order> orderOpt = orderRepository.findByMomoRequestId(momoRequestId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setPaymentStatus(Order.PAYMENT_STATUS_PAID);
            order.setMomoTransId(transId);
            order.setPaymentDate(LocalDateTime.now());
            order.setStatus(Order.STATUS_CONFIRMED);
            orderRepository.save(order);
        }
    }

    /**
     * Handle failed payment
     */
    public void handlePaymentFailed(String momoRequestId, String errorCode, String message) {
        Optional<Order> orderOpt = orderRepository.findByMomoRequestId(momoRequestId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setPaymentStatus(Order.PAYMENT_STATUS_FAILED);
            order.setStatus(Order.STATUS_CANCELLED);
            orderRepository.save(order);
        }
    }
}
