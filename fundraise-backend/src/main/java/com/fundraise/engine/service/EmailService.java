package com.fundraise.engine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:noreply@fundraise-readiness.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendComplianceCheckComplete(String toEmail, String companyName, int totalFindings, int criticalCount) {
        if (!emailEnabled) {
            log.info("Email disabled — skipping notification to {} for company {}", toEmail, companyName);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Compliance Check Complete — " + companyName);

            String severity = criticalCount > 0 ? "critical issues" : "no critical issues";
            message.setText(
                "Hi,\n\n" +
                "Your compliance check for " + companyName + " is complete.\n\n" +
                "Results:\n" +
                "• Total findings: " + totalFindings + "\n" +
                "• Critical issues: " + criticalCount + "\n\n" +
                (criticalCount > 0
                    ? "Action required: You have " + criticalCount + " critical issue(s) that need attention before fundraise.\n"
                    + "Log in to view the full gap report and priority actions.\n\n"
                    : "Looking good! No critical issues found. Review the full report for minor improvements.\n\n") +
                "View your report: https://fundraise-readiness.vercel.app/app\n\n" +
                "— Fundraise Readiness Engine"
            );

            mailSender.send(message);
            log.info("Compliance notification sent to {} for company {}", toEmail, companyName);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendScoreChanged(String toEmail, String companyName, String category, String oldGrade, String newGrade) {
        if (!emailEnabled) {
            log.info("Email disabled — skipping score change notification to {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Score Updated — " + companyName + " (" + category + ")");

            message.setText(
                "Hi,\n\n" +
                "Your readiness score for " + companyName + " has changed.\n\n" +
                "Category: " + category + "\n" +
                "Previous grade: " + oldGrade + "\n" +
                "New grade: " + newGrade + "\n\n" +
                "View your dashboard: https://fundraise-readiness.vercel.app/app\n\n" +
                "— Fundraise Readiness Engine"
            );

            mailSender.send(message);
            log.info("Score change notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send score change email to {}: {}", toEmail, e.getMessage());
        }
    }
}
