package sumdu.edu.ua.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.persistence.entity.CommentEntity;
import sumdu.edu.ua.persistence.repository.CommentRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final List<String> FORBIDDEN_WORDS = Arrays.asList("spam", "viagra", "casino");
    
    private final CommentRepositoryPort repo;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepositoryPort repo, CommentRepository commentRepository) {
        this.repo = repo;
        this.commentRepository = commentRepository;
    }

    public void validateCommentFields(long bookId, String author, String text) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("bookId must be greater than 0");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author is required");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }
    }

    /**
     * Validates comment text for length and forbidden words.
     * 
     * @param text the comment text to validate
     * @throws InvalidCommentTextException if text exceeds max length or contains forbidden words
     */
    public void validateCommentText(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidCommentTextException("Comment text cannot be empty");
        }
        
        if (text.length() > MAX_COMMENT_LENGTH) {
            throw new InvalidCommentTextException("Comment text exceeds maximum length of " + MAX_COMMENT_LENGTH + " characters");
        }
        
        String lowerText = text.toLowerCase();
        for (String forbiddenWord : FORBIDDEN_WORDS) {
            if (lowerText.contains(forbiddenWord)) {
                throw new InvalidCommentTextException("Comment contains forbidden word: " + forbiddenWord);
            }
        }
    }

    @Transactional
    public void addComment(long bookId, String author, String text) {
        validateCommentFields(bookId, author, text);
        validateCommentText(text);
        repo.add(bookId, author.trim(), text.trim());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(long bookId, long commentId, Instant createdAt) {
        if (bookId <= 0) {
            throw new InvalidCommentDeleteException("bookId must be greater than 0");
        }
        if (commentId <= 0) {
            throw new InvalidCommentDeleteException("commentId must be greater than 0");
        }
        if (createdAt == null) {
            throw new InvalidCommentDeleteException("createdAt is required");
        }

        if (Duration.between(createdAt, Instant.now()).toHours() > 24) {
            throw new CommentTooOldException("Comment was created more than 24 hours ago and cannot be deleted");
        }

        repo.delete(bookId, commentId);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByUser(Long userId) {
        List<CommentEntity> entities = commentRepository.findByUserId(userId);
        return entities.stream()
            .map(e -> new Comment(
                e.getId(),
                e.getBook().getId(),
                e.getUser().getId(),
                e.getBook().getTitle(),
                e.getUser().getEmail(),
                e.getText(),
                e.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
}
