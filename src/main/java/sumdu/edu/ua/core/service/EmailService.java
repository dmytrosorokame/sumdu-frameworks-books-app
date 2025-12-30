package sumdu.edu.ua.core.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.web.service.EmailTemplateProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateProcessor templateProcessor;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, EmailTemplateProcessor templateProcessor) {
        this.mailSender = mailSender;
        this.templateProcessor = templateProcessor;
    }

    /**
     * Sends confirmation email to the user.
     * @param toEmail recipient email address
     * @param firstName user's first name
     * @param confirmationCode confirmation code to send
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendConfirmationEmail(String toEmail, String firstName, String confirmationCode) {
        try {
            // Check if email is configured
            if (fromEmail == null || fromEmail.isEmpty() || fromEmail.contains("YOUR_GMAIL")) {
                log.warn("Email is not configured. Skipping email send. Confirmation code for {}: {}", 
                        toEmail, confirmationCode);
                log.warn("To confirm account, visit: {}/confirm?code={}", baseUrl, confirmationCode);
                return false; // Email not sent
            }

            String confirmationUrl = baseUrl + "/confirm?code=" + confirmationCode;
            String displayFirstName = firstName != null && !firstName.isEmpty() ? firstName : "User";

            // Prepare template model
            Map<String, Object> model = new HashMap<>();
            model.put("firstName", displayFirstName);
            model.put("confirmationUrl", confirmationUrl);
            model.put("confirmationCode", confirmationCode);

            // Process template
            String html = templateProcessor.process("confirmation_email.ftl", model);
            log.debug("Email template processed successfully");

            // Create MIME message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Registration Confirmation - Books Catalog");
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Confirmation email sent successfully to: {}", toEmail);
            return true; // Email sent successfully
        } catch (Exception e) {
            log.error("Failed to send confirmation email to: {}", toEmail, e);
            log.warn("Confirmation code for {}: {} - User can still confirm manually via /confirm?code={}", 
                    toEmail, confirmationCode, confirmationCode);
            return false; // Email not sent due to error
        }
    }

    public void sendNewBookEmail(Book book) {
        try {
            log.info("Preparing email for book: {} by {}", book.getTitle(), book.getAuthor());

            // Check if email is configured
            if (fromEmail == null || fromEmail.isEmpty() || fromEmail.contains("YOUR_GMAIL")) {
                log.warn("Email is not configured. Skipping new book notification email.");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            Map<String, Object> model = new HashMap<>();
            model.put("title", book.getTitle());
            model.put("author", book.getAuthor());
            model.put("year", book.getPubYear());
            model.put("added", now.format(formatter));

            String html = templateProcessor.process("new_book.ftl", model);
            log.debug("Email template processed successfully");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);

            helper.setFrom(fromEmail);
            helper.setTo(fromEmail); // Send to admin email
            helper.setSubject("New Book in Catalog");
            helper.setText(html, true);

            mailSender.send(message);
            log.info("New book email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email for book: {} by {}", book.getTitle(), book.getAuthor(), e);
            // Don't throw exception - allow book creation to succeed even if email fails
        }
    }
}

