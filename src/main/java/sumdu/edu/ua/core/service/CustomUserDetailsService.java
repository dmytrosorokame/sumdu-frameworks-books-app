package sumdu.edu.ua.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user by email address.
     * Note: Method name is from Spring Security interface, but we use email as identifier.
     * 
     * @param username actually the user's email address
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username; // In our system, username is email
        log.debug("Loading user by email: {}", email);

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found by email: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        log.debug("Found user: {} with role: {}, enabled: {}", 
                userEntity.getEmail(), userEntity.getRole(), userEntity.getEnabled());

        if (!userEntity.getEnabled()) {
            log.warn("User {} attempted to login but account is not confirmed", userEntity.getEmail());
        }

        // Create Spring Security User with role (prefixed with ROLE_)
        String role = userEntity.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return new User(
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getEnabled(),
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}

