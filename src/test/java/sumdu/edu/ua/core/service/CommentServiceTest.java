package sumdu.edu.ua.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.persistence.repository.CommentRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepositoryPort commentRepositoryPort;

    @Mock
    private CommentRepository commentRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepositoryPort, commentRepository);
    }

    @Test
    void delete_withInvalidBookId_throwsInvalidCommentDeleteException() {
        long invalidBookId = 0;
        long commentId = 1;
        Instant createdAt = Instant.now();

        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(invalidBookId, commentId, createdAt)
        );

        assertEquals("bookId must be greater than 0", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withNegativeBookId_throwsInvalidCommentDeleteException() {
        long negativeBookId = -5;
        long commentId = 1;
        Instant createdAt = Instant.now();

        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(negativeBookId, commentId, createdAt)
        );

        assertEquals("bookId must be greater than 0", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withInvalidCommentId_throwsInvalidCommentDeleteException() {
        long bookId = 1;
        long invalidCommentId = 0;
        Instant createdAt = Instant.now();

        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, invalidCommentId, createdAt)
        );

        assertEquals("commentId must be greater than 0", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withNegativeCommentId_throwsInvalidCommentDeleteException() {
        long bookId = 1;
        long negativeCommentId = -10;
        Instant createdAt = Instant.now();

        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, negativeCommentId, createdAt)
        );

        assertEquals("commentId must be greater than 0", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withNullCreatedAt_throwsInvalidCommentDeleteException() {
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = null;

        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertEquals("createdAt is required", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withCreatedAtOlderThan24Hours_throwsCommentTooOldException() {
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(25, ChronoUnit.HOURS);

        CommentTooOldException exception = assertThrows(
                CommentTooOldException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertEquals("Comment was created more than 24 hours ago and cannot be deleted", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withCreatedAtJustOver24Hours_throwsCommentTooOldException() {
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(25, ChronoUnit.HOURS);

        CommentTooOldException exception = assertThrows(
                CommentTooOldException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertEquals("Comment was created more than 24 hours ago and cannot be deleted", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void delete_withCreatedAtExactly24HoursAgo_callsRepositoryDelete() {
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(24, ChronoUnit.HOURS);

        commentService.delete(bookId, commentId, createdAt);

        verify(commentRepositoryPort).delete(bookId, commentId);
    }

    @Test
    void delete_withValidData_callsRepositoryDelete() {
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(1, ChronoUnit.HOURS);

        commentService.delete(bookId, commentId, createdAt);

        verify(commentRepositoryPort).delete(bookId, commentId);
    }

    @Test
    void delete_withCreatedAtJustNow_callsRepositoryDelete() {
        long bookId = 5;
        long commentId = 10;
        Instant createdAt = Instant.now();

        commentService.delete(bookId, commentId, createdAt);

        verify(commentRepositoryPort).delete(bookId, commentId);
    }

    @Test
    void delete_withCreatedAt23HoursAgo_callsRepositoryDelete() {
        long bookId = 3;
        long commentId = 7;
        Instant createdAt = Instant.now().minus(23, ChronoUnit.HOURS);

        commentService.delete(bookId, commentId, createdAt);

        verify(commentRepositoryPort).delete(bookId, commentId);
    }

    // ========== Tests for validateCommentText() ==========

    @Test
    void validateCommentText_withNullText_throwsInvalidCommentTextException() {
        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText(null)
        );

        assertEquals("Comment text cannot be empty", exception.getMessage());
    }

    @Test
    void validateCommentText_withEmptyText_throwsInvalidCommentTextException() {
        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText("")
        );

        assertEquals("Comment text cannot be empty", exception.getMessage());
    }

    @Test
    void validateCommentText_withBlankText_throwsInvalidCommentTextException() {
        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText("   ")
        );

        assertEquals("Comment text cannot be empty", exception.getMessage());
    }

    @Test
    void validateCommentText_withTextExceedingMaxLength_throwsInvalidCommentTextException() {
        String longText = "a".repeat(1001);

        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText(longText)
        );

        assertEquals("Comment text exceeds maximum length of 1000 characters", exception.getMessage());
    }

    @Test
    void validateCommentText_withTextAtMaxLength_passes() {
        String maxLengthText = "a".repeat(1000);

        assertDoesNotThrow(() -> commentService.validateCommentText(maxLengthText));
    }

    @Test
    void validateCommentText_withForbiddenWordSpam_throwsInvalidCommentTextException() {
        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText("This is spam content")
        );

        assertEquals("Comment contains forbidden word: spam", exception.getMessage());
    }

    @Test
    void validateCommentText_withForbiddenWordViagra_throwsInvalidCommentTextException() {
        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText("Buy VIAGRA now!")
        );

        assertEquals("Comment contains forbidden word: viagra", exception.getMessage());
    }

    @Test
    void validateCommentText_withForbiddenWordCasino_throwsInvalidCommentTextException() {
        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.validateCommentText("Win at casino today")
        );

        assertEquals("Comment contains forbidden word: casino", exception.getMessage());
    }

    @Test
    void validateCommentText_withValidText_passes() {
        assertDoesNotThrow(() -> commentService.validateCommentText("This is a great book!"));
    }

    // ========== Tests for addComment() with text validation ==========

    @Test
    void addComment_withForbiddenWord_throwsInvalidCommentTextException() {
        long bookId = 1;
        String author = "user@example.com";
        String text = "This is spam!";

        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.addComment(bookId, author, text)
        );

        assertEquals("Comment contains forbidden word: spam", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void addComment_withTextTooLong_throwsInvalidCommentTextException() {
        long bookId = 1;
        String author = "user@example.com";
        String text = "a".repeat(1001);

        InvalidCommentTextException exception = assertThrows(
                InvalidCommentTextException.class,
                () -> commentService.addComment(bookId, author, text)
        );

        assertEquals("Comment text exceeds maximum length of 1000 characters", exception.getMessage());
        verifyNoInteractions(commentRepositoryPort);
    }

    @Test
    void addComment_withValidData_callsRepository() {
        long bookId = 1;
        String author = "user@example.com";
        String text = "Great book!";

        commentService.addComment(bookId, author, text);

        verify(commentRepositoryPort).add(bookId, author, text);
    }
}

