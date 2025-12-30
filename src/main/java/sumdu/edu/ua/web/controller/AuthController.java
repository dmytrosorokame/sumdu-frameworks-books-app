package sumdu.edu.ua.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sumdu.edu.ua.core.service.UserService;

import java.util.Locale;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public AuthController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    private String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, locale);
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", getMessage("auth.login.error"));
            log.warn("Login failed - invalid credentials");
        }
        if (logout != null) {
            model.addAttribute("message", getMessage("auth.login.logout.success"));
            log.info("User logged out successfully");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        log.info("Registration attempt for email: {}", email);

        // Validation
        if (firstName == null || firstName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", getMessage("auth.register.error.firstname.empty"));
            return "redirect:/register";
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", getMessage("auth.register.error.lastname.empty"));
            return "redirect:/register";
        }

        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", getMessage("auth.register.error.email.empty"));
            return "redirect:/register";
        }

        if (password == null || password.length() < 4) {
            redirectAttributes.addFlashAttribute("error", getMessage("auth.register.error.password.short"));
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", getMessage("auth.register.error.password.mismatch"));
            return "redirect:/register";
        }

        try {
            userService.registerNewUser(email.trim(), password, firstName.trim(), lastName.trim());
            log.info("User registered successfully: {}", email);
            redirectAttributes.addFlashAttribute("messageKey", "auth.register.success.email.sent");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam(required = false) String code,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (code == null || code.trim().isEmpty()) {
            model.addAttribute("error", getMessage("auth.confirm.error.code.missing"));
            return "auth/confirm";
        }

        try {
            boolean confirmed = userService.confirmEmail(code.trim());
            if (confirmed) {
                redirectAttributes.addFlashAttribute("messageKey", "auth.confirm.success");
                return "redirect:/login";
            } else {
                model.addAttribute("error", getMessage("auth.confirm.error.invalid"));
                return "auth/confirm";
            }
        } catch (Exception e) {
            log.error("Error confirming email: {}", e.getMessage(), e);
            model.addAttribute("error", getMessage("auth.confirm.error.general"));
            return "auth/confirm";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }

    @GetMapping("/404")
    public String notFound() {
        return "error/404";
    }
}

