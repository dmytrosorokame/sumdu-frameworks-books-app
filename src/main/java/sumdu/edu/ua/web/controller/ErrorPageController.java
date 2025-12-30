package sumdu.edu.ua.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for displaying error pages.
 */
@Controller
@RequestMapping("/error")
public class ErrorPageController {

    @GetMapping("/general")
    public String showGeneralError(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorTitleKey,
            @RequestParam(required = false) String errorMessage,
            Model model) {
        // URL params have priority over flash attributes
        if (errorCode != null) {
            model.addAttribute("errorCode", errorCode);
        }
        if (errorTitleKey != null) {
            model.addAttribute("errorTitleKey", errorTitleKey);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "error/general-error";
    }
}

