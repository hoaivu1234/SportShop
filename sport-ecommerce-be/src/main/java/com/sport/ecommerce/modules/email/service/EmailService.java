package com.sport.ecommerce.modules.email.service;

import com.sport.ecommerce.modules.email.dto.EmailMessage;
import com.sport.ecommerce.modules.email.template.EmailTemplate;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderSuccessEmail(EmailMessage msg) {
        String subject = "Order Confirmation #" + msg.getOrderId();

        String content = EmailTemplate.buildOrderTemplate(msg);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            log.info("Sending email to {}", msg.getTo());
            helper.setFrom(msg.getFrom());
            helper.setTo(msg.getTo());
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
