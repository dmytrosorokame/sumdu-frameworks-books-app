package sumdu.edu.ua.core.exception;

/**
 * Exception thrown when comment text validation fails.
 * For example: text is too long or contains forbidden words.
 */
public class InvalidCommentTextException extends RuntimeException {
    
    public InvalidCommentTextException(String message) {
        super(message);
    }
}


