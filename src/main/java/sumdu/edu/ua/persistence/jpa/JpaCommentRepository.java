package sumdu.edu.ua.persistence.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.persistence.entity.BookEntity;
import sumdu.edu.ua.persistence.entity.CommentEntity;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.BookRepository;
import sumdu.edu.ua.persistence.repository.CommentRepository;
import sumdu.edu.ua.persistence.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Primary
public class JpaCommentRepository implements CommentRepositoryPort {

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public JpaCommentRepository(CommentRepository commentRepository,
                                BookRepository bookRepository,
                                UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void add(long bookId, String author, String text) {
        BookEntity book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));
        
        UserEntity user = userRepository.findByEmail(author)
            .orElseGet(() -> {
                // Create a temporary user with email as identifier
                // Note: This creates an unconfirmed user - in production you might want to handle this differently
                UserEntity newUser = new UserEntity(author, "default", "USER");
                newUser.setEnabled(true); // Enable for comments to work
                return userRepository.save(newUser);
            });

        CommentEntity comment = new CommentEntity(book, user, text);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(
            request.getPage(),
            request.getSize()
        );

        org.springframework.data.domain.Page<CommentEntity> page = commentRepository.findByBookIdAndFilters(
            bookId,
            author,
            since,
            springPageRequest
        );

        List<Comment> comments = page.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());

        return new Page<>(comments, request, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Comment findById(long bookId, long commentId) {
        return commentRepository.findById(commentId)
            .filter(c -> c.getBook().getId().equals(bookId))
            .map(this::toDomain)
            .orElse(null);
    }

    @Override
    @Transactional
    public void delete(long bookId, long commentId) {
        commentRepository.findById(commentId)
            .filter(c -> c.getBook().getId().equals(bookId))
            .ifPresent(commentRepository::delete);
    }

    private Comment toDomain(CommentEntity entity) {
        return new Comment(
            entity.getId(),
            entity.getBook().getId(),
            entity.getUser().getId(),
            entity.getBook().getTitle(),
            entity.getUser().getEmail(),
            entity.getText(),
            entity.getCreatedAt()
        );
    }
}

