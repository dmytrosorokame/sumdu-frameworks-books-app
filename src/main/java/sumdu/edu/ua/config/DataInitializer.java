package sumdu.edu.ua.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.UserRepository;

/**
 * Initializes default users with proper BCrypt passwords on application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@example.com";
        
        // Create admin user if not exists
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            String adminPassword = passwordEncoder.encode("admin");
            UserEntity admin = new UserEntity(adminEmail, adminPassword, "ADMIN", "Admin", "User");
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Created admin user: {} with password: admin", adminEmail);
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }

        log.info("Data initialization completed");
    }
}

