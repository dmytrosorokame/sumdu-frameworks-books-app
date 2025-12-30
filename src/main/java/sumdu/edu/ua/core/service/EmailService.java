package sumdu.edu.ua.core.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final EmailTemplateProcessor templateProcessor;
    private final Resend resend;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    public EmailService(EmailTemplateProcessor templateProcessor,
                        @Value("${resend.api.key:}") String apiKey) {
        this.templateProcessor = templateProcessor;
        this.resend = apiKey != null && !apiKey.isEmpty() ? new Resend(apiKey) : null;
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
            // Check if Resend is configured
            if (resend == null) {
                log.warn("Resend API is not configured. Skipping email send. Confirmation code for {}: {}", 
                        toEmail, confirmationCode);
                log.warn("To confirm account, visit: {}/confirm?code={}", baseUrl, confirmationCode);
                return false;
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

            // Send via Resend
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject("Registration Confirmation - Books Catalog")
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Confirmation email sent successfully to: {} (id: {})", toEmail, response.getId());
            return true;
        } catch (ResendException e) {
            log.error("Failed to send confirmation email to: {}", toEmail, e);
            log.warn("Confirmation code for {}: {} - User can still confirm manually via /confirm?code={}", 
                    toEmail, confirmationCode, confirmationCode);
            return false;
        }
    }

    public void sendNewBookEmail(Book book) {
        try {
            log.info("Preparing email for book: {} by {}", book.getTitle(), book.getAuthor());

            // Check if Resend is configured
            if (resend == null) {
                log.warn("Resend API is not configured. Skipping new book notification email.");
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

            // Send via Resend
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(adminEmail)
                    .subject("New Book in Catalog")
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("New book email sent successfully (id: {})", response.getId());
        } catch (ResendException e) {
            log.error("Failed to send email for book: {} by {}", book.getTitle(), book.getAuthor(), e);
            // Don't throw exception - allow book creation to succeed even if email fails
        }
    }
}
