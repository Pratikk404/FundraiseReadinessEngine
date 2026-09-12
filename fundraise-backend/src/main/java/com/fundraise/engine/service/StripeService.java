package com.fundraise.engine.service;

import com.fundraise.engine.config.StripeConfig;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final StripeConfig stripeConfig;
    private final UserRepository userRepository;

    public String createCheckoutSession(User user, String plan) throws StripeException {
        String priceId;
        User.Plan userPlan;

        switch (plan.toLowerCase()) {
            case "pro":
                priceId = stripeConfig.getProPriceId();
                userPlan = User.Plan.PRO;
                break;
            case "advisor":
                priceId = stripeConfig.getAdvisorPriceId();
                userPlan = User.Plan.ADVISOR;
                break;
            default:
                throw new IllegalArgumentException("Invalid plan: " + plan);
        }

        if (priceId == null || priceId.isBlank()) {
            throw new IllegalStateException("Stripe price ID not configured for plan: " + plan);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomerEmail(user.getEmail())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .putMetadata("userId", user.getId().toString())
                .putMetadata("plan", userPlan.name())
                .setSuccessUrl(stripeConfig.getFrontendUrl() + "/app/settings?upgraded=true")
                .setCancelUrl(stripeConfig.getFrontendUrl() + "/pricing")
                .build();

        Session session = Session.create(params);
        log.info("Stripe checkout session created for user {} — plan {}", user.getEmail(), plan);
        return session.getUrl();
    }

    public void handleWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        String webhookSecret = System.getenv("STRIPE_WEBHOOK_SECRET");
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("STRIPE_WEBHOOK_SECRET not set — skipping webhook verification");
            return;
        }

        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    String userId = session.getMetadata().get("userId");
                    String plan = session.getMetadata().get("plan");

                    if (userId != null && plan != null) {
                        userRepository.findById(java.util.UUID.fromString(userId)).ifPresent(user -> {
                            user.setPlan(User.Plan.valueOf(plan));
                            userRepository.save(user);
                            log.info("User {} upgraded to plan {}", user.getEmail(), plan);
                        });
                    }
                }
            } catch (Exception e) {
                log.error("Error processing checkout.session.completed event: {}", e.getMessage());
            }
        }
    }
}
