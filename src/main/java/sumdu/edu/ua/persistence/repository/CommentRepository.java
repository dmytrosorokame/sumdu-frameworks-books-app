package sumdu.edu.ua.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sumdu.edu.ua.persistence.entity.CommentEntity;

import java.time.Instant;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByUserId(Long userId);

    @Query(value = "SELECT c.* FROM comments c " +
                   "JOIN users u ON u.id = c.user_id " +
                   "WHERE c.book_id = :bookId " +
                   "AND (CAST(:author AS VARCHAR) IS NULL OR CAST(:author AS VARCHAR) = '' OR u.email LIKE '%' || CAST(:author AS VARCHAR) || '%') " +
                   "AND (CAST(:since AS TIMESTAMP) IS NULL OR c.created_at >= CAST(:since AS TIMESTAMP)) " +
                   "ORDER BY c.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM comments c " +
                   "JOIN users u ON u.id = c.user_id " +
                   "WHERE c.book_id = :bookId " +
                   "AND (CAST(:author AS VARCHAR) IS NULL OR CAST(:author AS VARCHAR) = '' OR u.email LIKE '%' || CAST(:author AS VARCHAR) || '%') " +
                   "AND (CAST(:since AS TIMESTAMP) IS NULL OR c.created_at >= CAST(:since AS TIMESTAMP))",
           nativeQuery = true)
    Page<CommentEntity> findByBookIdAndFilters(@Param("bookId") Long bookId,
                                                @Param("author") String author,
                                                @Param("since") Instant since,
                                                Pageable pageable);
}
