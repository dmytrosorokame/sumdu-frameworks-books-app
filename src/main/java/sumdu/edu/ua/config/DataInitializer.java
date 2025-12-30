package sumdu.edu.ua.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.UserRepository;

/**
 * Initializes default users with proper BCrypt passwords on application startup.
 * Admin credentials can be configured via environment variables:
 * - APP_ADMIN_EMAIL (default: admin@example.com)
 * - APP_ADMIN_PASSWORD (default: admin)
 * - APP_ADMIN_FIRST_NAME (default: Admin)
 * - APP_ADMIN_LAST_NAME (default: User)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    @Value("${app.admin.first-name:Admin}")
    private String adminFirstName;

    @Value("${app.admin.last-name:User}")
    private String adminLastName;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create admin user if not exists
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            String encodedPassword = passwordEncoder.encode(adminPassword);
            UserEntity admin = new UserEntity(adminEmail, encodedPassword, "ADMIN", adminFirstName, adminLastName);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Created admin user: {} with configured password", adminEmail);
            log.warn("IMPORTANT: Change the default admin password in production!");
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }

        log.info("Data initialization completed");
    }
}

