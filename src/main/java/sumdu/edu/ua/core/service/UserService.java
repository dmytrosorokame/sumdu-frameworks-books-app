package sumdu.edu.ua.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public UserEntity findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public UserEntity registerNewUser(String email, String password, String firstName, String lastName) {
        log.info("Registering new user with email: {}", email);

        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        String encodedPassword = passwordEncoder.encode(password);
        String confirmationCode = generateConfirmationCode();
        
        UserEntity newUser = new UserEntity(email.trim(), encodedPassword, "USER", 
                firstName != null ? firstName.trim() : null,
                lastName != null ? lastName.trim() : null);
        newUser.setConfirmationCode(confirmationCode);
        newUser.setEnabled(false);

        UserEntity savedUser = userRepository.save(newUser);
        log.info("User registered successfully with email: {} and id: {}", 
                savedUser.getEmail(), savedUser.getId());

        // Send confirmation email
        boolean emailSent = emailService.sendConfirmationEmail(savedUser.getEmail(), 
                savedUser.getFirstName() != null ? savedUser.getFirstName() : savedUser.getEmail(),
                confirmationCode);
        
        if (emailSent) {
            log.info("Confirmation email sent to: {}", savedUser.getEmail());
        } else {
            log.warn("Failed to send confirmation email to: {}", savedUser.getEmail());
        }

        return savedUser;
    }

    @Transactional
    public boolean confirmEmail(String confirmationCode) {
        log.info("Attempting to confirm email with code: {}", confirmationCode);

        Optional<UserEntity> userOpt = userRepository.findByConfirmationCode(confirmationCode);
        
        if (userOpt.isEmpty()) {
            log.warn("Invalid confirmation code: {}", confirmationCode);
            return false;
        }

        UserEntity user = userOpt.get();
        
        if (user.getEnabled()) {
            log.warn("User {} is already confirmed", user.getEmail());
            return false;
        }

        user.setEnabled(true);
        user.setConfirmationCode(null); // Clear confirmation code after successful confirmation
        userRepository.save(user);
        
        log.info("Email confirmed successfully for user: {}", user.getEmail());
        return true;
    }

    private String generateConfirmationCode() {
        // Generate a random 32-character alphanumeric code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Simple email validation - check for @ and basic format
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    @Transactional
    public UserEntity registerAdmin(String email, String password) {
        log.info("Registering new admin: {}", email);

        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        String encodedPassword = passwordEncoder.encode(password);
        UserEntity newAdmin = new UserEntity(email.trim(), encodedPassword, "ADMIN");
        newAdmin.setEnabled(true); // Admin accounts are enabled by default

        return userRepository.save(newAdmin);
    }
}


