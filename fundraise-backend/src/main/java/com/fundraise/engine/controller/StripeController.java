package com.fundraise.engine.controller;

import com.fundraise.engine.dto.CheckoutRequest;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe", description = "Payment and subscription management")
@Slf4j
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    @Operation(summary = "Create checkout session", description = "Create a Stripe checkout session for upgrading plan.")
    public ResponseEntity<Map<String, String>> createCheckout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request) throws Exception {
        User user = (User) authentication.getPrincipal();
        String url = stripeService.createCheckoutSession(user, request.getPlan());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook", description = "Handle Stripe webhook events for subscription updates.")
    public ResponseEntity<String> webhook(HttpServletRequest request) {
        try {
            String payload = request.getReader().lines().reduce("", (a, b) -> a + b);
            String sigHeader = request.getHeader("Stripe-Signature");

            stripeService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("error");
        }
    }
}
