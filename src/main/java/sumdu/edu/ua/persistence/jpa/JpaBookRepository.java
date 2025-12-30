package sumdu.edu.ua.persistence.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.persistence.entity.BookEntity;
import sumdu.edu.ua.persistence.repository.BookRepository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Primary
public class JpaBookRepository implements CatalogRepositoryPort {

    private final BookRepository bookRepository;

    @Autowired
    public JpaBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> search(String q, PageRequest request) {
        List<BookEntity> allBooks = bookRepository.findAll();
        
        List<BookEntity> filtered = allBooks.stream()
            .filter(book -> {
                if (q == null || q.isBlank()) {
                    return true;
                }
                String lowerQ = q.toLowerCase();
                return book.getTitle().toLowerCase().contains(lowerQ) ||
                       book.getAuthor().toLowerCase().contains(lowerQ);
            })
            .collect(Collectors.toList());

        String sortBy = request.getSortBy();
        if (sortBy != null) {
            Sort sort = Sort.by(request.isSortDesc() ? Sort.Direction.DESC : Sort.Direction.ASC, 
                               mapSortField(sortBy));
            filtered.sort((a, b) -> {
                Comparable valA = getSortValue(a, sortBy);
                Comparable valB = getSortValue(b, sortBy);
                int result = valA.compareTo(valB);
                return request.isSortDesc() ? -result : result;
            });
        }

        int total = filtered.size();
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), total);
        List<BookEntity> paged = start < total ? filtered.subList(start, end) : List.of();

        List<Book> books = paged.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());

        return new Page<>(books, request, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Book findById(long id) {
        return bookRepository.findById(id)
            .map(this::toDomain)
            .orElse(null);
    }

    @Override
    @Transactional
    public Book add(String title, String author, int pubYear) {
        BookEntity entity = new BookEntity(title, author, pubYear);
        BookEntity saved = bookRepository.save(entity);
        return toDomain(saved);
    }

    private Book toDomain(BookEntity entity) {
        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), entity.getPubYear());
    }

    private String mapSortField(String sortBy) {
        if (sortBy == null) return "id";
        switch (sortBy.toLowerCase()) {
            case "title": return "title";
            case "author": return "author";
            case "year":
            case "pub_year": return "pubYear";
            default: return "id";
        }
    }

    @SuppressWarnings("unchecked")
    private Comparable getSortValue(BookEntity book, String sortBy) {
        if (sortBy == null) return book.getId();
        switch (sortBy.toLowerCase()) {
            case "title": return book.getTitle();
            case "author": return book.getAuthor();
            case "year":
            case "pub_year": return book.getPubYear();
            default: return book.getId();
        }
    }
}

