package sumdu.edu.ua.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sumdu.edu.ua.core.exception.BookNotFoundException;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;

/**
 * MVC Controller Advice for handling exceptions and redirecting to error pages.
 * This handler is specifically for MVC (HTML) controllers, not REST API controllers.
 * Has highest precedence to be checked before GlobalApiExceptionHandler.
 */
@ControllerAdvice(assignableTypes = {CommentsController.class, BooksController.class, UserCommentsController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MvcExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MvcExceptionHandler.class);

    @ExceptionHandler(InvalidCommentDeleteException.class)
    public String handleInvalidCommentDeleteException(InvalidCommentDeleteException ex, 
                                                       RedirectAttributes redirectAttributes) {
        log.warn("MVC: Invalid comment delete request: {}", ex.getMessage());
        redirectAttributes.addAttribute("errorCode", "400");
        redirectAttributes.addAttribute("errorTitleKey", "error.comment.delete.title");
        redirectAttributes.addAttribute("errorMessage", ex.getMessage());
        return "redirect:/error/general";
    }

    @ExceptionHandler(CommentTooOldException.class)
    public String handleCommentTooOldException(CommentTooOldException ex, 
                                                RedirectAttributes redirectAttributes) {
        log.warn("MVC: Comment too old for deletion: {}", ex.getMessage());
        redirectAttributes.addAttribute("errorCode", "400");
        redirectAttributes.addAttribute("errorTitleKey", "error.comment.too.old.title");
        redirectAttributes.addAttribute("errorMessage", ex.getMessage());
        return "redirect:/error/general";
    }

    @ExceptionHandler(BookNotFoundException.class)
    public String handleBookNotFoundException(BookNotFoundException ex, 
                                               RedirectAttributes redirectAttributes) {
        log.info("MVC: Book not found: {}", ex.getMessage());
        redirectAttributes.addAttribute("errorCode", "404");
        redirectAttributes.addAttribute("errorTitleKey", "error.book.not.found.title");
        redirectAttributes.addAttribute("errorMessage", ex.getMessage());
        return "redirect:/error/general";
    }

    @ExceptionHandler(InvalidCommentTextException.class)
    public String handleInvalidCommentTextException(InvalidCommentTextException ex, 
                                                     RedirectAttributes redirectAttributes) {
        log.warn("MVC: Invalid comment text: {}", ex.getMessage());
        redirectAttributes.addAttribute("errorCode", "400");
        redirectAttributes.addAttribute("errorTitleKey", "error.comment.text.invalid.title");
        redirectAttributes.addAttribute("errorMessage", ex.getMessage());
        return "redirect:/error/general";
    }
}

