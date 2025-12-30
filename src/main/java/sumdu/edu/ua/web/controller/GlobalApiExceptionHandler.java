package sumdu.edu.ua.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sumdu.edu.ua.core.exception.BookNotFoundException;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;

import java.util.Map;

/**
 * REST API Exception Handler for API controllers only.
 * Returns JSON error responses.
 */
@RestControllerAdvice(assignableTypes = {CommentsApiController.class, BooksApiController.class})
@Order(1)
public class GlobalApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(InvalidCommentDeleteException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCommentDeleteException(InvalidCommentDeleteException ex) {
        log.warn("Invalid comment delete request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CommentTooOldException.class)
    public ResponseEntity<Map<String, String>> handleCommentTooOldException(CommentTooOldException ex) {
        log.warn("Comment too old for deletion: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookNotFoundException(BookNotFoundException ex) {
        log.info("Book not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCommentTextException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCommentTextException(InvalidCommentTextException ex) {
        log.warn("Invalid comment text: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}

