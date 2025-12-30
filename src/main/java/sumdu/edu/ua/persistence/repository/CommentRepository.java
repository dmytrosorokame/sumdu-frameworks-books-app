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

    @Query("SELECT c FROM CommentEntity c WHERE c.book.id = :bookId " +
           "AND (:author IS NULL OR :author = '' OR c.user.email LIKE CONCAT('%', :author, '%')) " +
           "AND (:since IS NULL OR c.createdAt >= :since) " +
           "ORDER BY c.createdAt DESC")
    Page<CommentEntity> findByBookIdAndFilters(@Param("bookId") Long bookId,
                                                @Param("author") String author,
                                                @Param("since") Instant since,
                                                Pageable pageable);
}


