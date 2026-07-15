package com.ralsei.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * Central utility for sending UTF-8 HTML email with optional inline images.
 * SMTP credentials remain in external configuration and are never accepted as
 * method arguments, embedded in source code, or written to application logs.
 */
@Component
@RequiredArgsConstructor
/**
 * Provides utility helpers for email uti processing.
 */
public class EmailUtility {

    private static final String PNG_CONTENT_TYPE = "image/png";

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String senderAddress;

    @Value("${app.mail.sender-name:Nhà xe Tuấn MV}")
    private String senderName;

    /**
     * Sends one HTML message and embeds each PNG under its supplied content ID.
     *
     * @param recipient validated customer email address
     * @param subject concise message subject
     * @param htmlBody fully rendered HTML body
     * @param inlinePngImages map of content ID to PNG bytes
     * @throws IllegalArgumentException when required message data is missing
     * @throws IllegalStateException when the mail message cannot be constructed
     */
    public void sendHtml(
            String recipient,
            String subject,
            String htmlBody,
            Map<String, byte[]> inlinePngImages) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Email người nhận không được để trống.");
        }
        if (subject == null || subject.isBlank() || htmlBody == null || htmlBody.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề và nội dung email không được để trống.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                StandardCharsets.UTF_8.name()
            );
            helper.setFrom(senderAddress, senderName);
            helper.setTo(recipient.trim());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            Map<String, byte[]> safeInlineImages = inlinePngImages == null
                ? Collections.emptyMap()
                : inlinePngImages;
            safeInlineImages.forEach((contentId, bytes) -> {
                try {
                    helper.addInline(contentId, new ByteArrayResource(bytes), PNG_CONTENT_TYPE);
                } catch (MessagingException exception) {
                    throw new InlineImageException(exception);
                }
            });
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException exception) {
            throw new IllegalStateException("Không thể tạo email xác nhận vé.", exception);
        } catch (InlineImageException exception) {
            throw new IllegalStateException("Không thể đính kèm mã QR vào email.", exception.getCause());
        }
    }

    /** Wraps checked MIME failures raised inside the inline-image lambda. */
    private static final class InlineImageException extends RuntimeException {
        private InlineImageException(MessagingException cause) {
            super(cause);
        }
    }
}
