package vn.hutech.trandinhty_2280618597.controllers;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import vn.hutech.trandinhty_2280618597.config.MoMoConfig;
import vn.hutech.trandinhty_2280618597.entities.Cart;
import vn.hutech.trandinhty_2280618597.entities.Order;
import vn.hutech.trandinhty_2280618597.entities.User;
import vn.hutech.trandinhty_2280618597.services.CartService;
import vn.hutech.trandinhty_2280618597.services.MoMoService;
import vn.hutech.trandinhty_2280618597.services.OrderService;
import vn.hutech.trandinhty_2280618597.services.UserService;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MoMoService moMoService;

    @Autowired
    private MoMoConfig moMoConfig;

    /**
     * Display checkout page with payment method selection
     */
    @GetMapping
    public String showCheckout(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(user.getId());
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        return "checkout/checkout";
    }

    /**
     * Process checkout with selected payment method
     */
    @PostMapping
    public String processCheckout(@RequestParam String paymentMethod, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(user.getId());
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Create order with selected payment method
        Order order = orderService.createOrderWithPaymentMethod(
                user.getId(),
                user.getUsername(),
                paymentMethod);

        if (order == null) {
            return "redirect:/cart?error=empty";
        }

        // Handle payment based on method
        if (Order.PAYMENT_METHOD_MOMO.equals(paymentMethod)) {
            try {
                // Create MoMo payment request
                String payUrl = moMoService.createPaymentRequest(
                        order.getId(),
                        (long) order.getTotalAmount(),
                        "Thanh toán đơn hàng #" + order.getId(),
                        order.getMomoRequestId());
                // Redirect to MoMo payment page
                return "redirect:" + payUrl;
            } catch (Exception e) {
                // Handle MoMo error
                order.setPaymentStatus(Order.PAYMENT_STATUS_FAILED);
                order.setStatus(Order.STATUS_CANCELLED);
                return "redirect:/checkout/failed?orderId=" + order.getId() + "&error=" + e.getMessage();
            }
        } else {
            // COD - redirect to order confirmation
            return "redirect:/orders/" + order.getId();
        }
    }

    /**
     * Handle MoMo return callback (redirect from MoMo after payment)
     */
    @GetMapping("/momo/return")
    public String momoReturn(@RequestParam Map<String, String> params, Model model) {
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");
        String requestId = params.get("requestId");
        String transId = params.get("transId");
        String message = params.get("message");
        String signature = params.get("signature");

        // Build raw signature for verification
        String rawSignature = moMoService.buildCallbackRawSignature(
                moMoConfig.getAccessKey(),
                params.get("amount"),
                params.getOrDefault("extraData", ""),
                message,
                orderId,
                params.get("orderInfo"),
                params.getOrDefault("orderType", ""),
                moMoConfig.getPartnerCode(),
                params.getOrDefault("payType", ""),
                requestId,
                params.get("responseTime"),
                resultCode,
                transId);

        // Verify signature
        boolean isValid = moMoService.verifySignature(signature, rawSignature);

        if (!isValid) {
            model.addAttribute("error", "Chữ ký không hợp lệ");
            return "checkout/failed";
        }

        // Check result code
        if ("0".equals(resultCode)) {
            // Payment successful
            moMoService.handlePaymentSuccess(requestId, transId);

            Optional<Order> orderOpt = orderService.findByMomoRequestId(requestId);
            if (orderOpt.isPresent()) {
                model.addAttribute("order", orderOpt.get());
            }
            return "checkout/success";
        } else {
            // Payment failed
            moMoService.handlePaymentFailed(requestId, resultCode, message);
            model.addAttribute("error", message);
            model.addAttribute("errorCode", resultCode);
            return "checkout/failed";
        }
    }

    /**
     * Handle MoMo IPN callback (server-to-server notification)
     */
    @PostMapping("/momo/ipn")
    @ResponseBody
    public String momoIpn(@RequestParam Map<String, String> params) {
        String resultCode = params.get("resultCode");
        String requestId = params.get("requestId");
        String transId = params.get("transId");
        String message = params.get("message");
        String signature = params.get("signature");

        // Build raw signature for verification
        String rawSignature = moMoService.buildCallbackRawSignature(
                moMoConfig.getAccessKey(),
                params.get("amount"),
                params.getOrDefault("extraData", ""),
                message,
                params.get("orderId"),
                params.get("orderInfo"),
                params.getOrDefault("orderType", ""),
                moMoConfig.getPartnerCode(),
                params.getOrDefault("payType", ""),
                requestId,
                params.get("responseTime"),
                resultCode,
                transId);

        // Verify signature
        boolean isValid = moMoService.verifySignature(signature, rawSignature);

        if (!isValid) {
            return "-1"; // Invalid signature
        }

        // Process payment result
        if ("0".equals(resultCode)) {
            moMoService.handlePaymentSuccess(requestId, transId);
        } else {
            moMoService.handlePaymentFailed(requestId, resultCode, message);
        }

        return "0"; // Success response for MoMo
    }

    /**
     * Payment success page
     */
    @GetMapping("/success")
    public String successPage(@RequestParam(required = false) String orderId, Model model) {
        if (orderId != null) {
            orderService.getOrderById(orderId).ifPresent(order -> model.addAttribute("order", order));
        }
        return "checkout/success";
    }

    /**
     * Payment failed page
     */
    @GetMapping("/failed")
    public String failedPage(@RequestParam(required = false) String orderId,
            @RequestParam(required = false) String error,
            Model model) {
        if (orderId != null) {
            orderService.getOrderById(orderId).ifPresent(order -> model.addAttribute("order", order));
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "checkout/failed";
    }
}
