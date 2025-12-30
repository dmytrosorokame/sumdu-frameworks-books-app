package sumdu.edu.ua.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;

import java.time.Instant;

@Controller
@RequestMapping("/books/{bookId}")
public class CommentsController {

    private static final Logger log = LoggerFactory.getLogger(CommentsController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentRepositoryPort commentRepo;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public String showBookWithComments(
            @PathVariable long bookId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            Model model) {

        Book book = bookService.findById(bookId);
        if (book == null) {
            log.warn("Book not found: {}", bookId);
            return "redirect:/books";
        }

        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }

        PageRequest pageRequest = new PageRequest(page, size);
        Page<Comment> result = commentRepo.list(bookId, null, null, pageRequest);

        long total = result.getTotal();
        int totalPages = (int) ((total + size - 1) / size);

        model.addAttribute("book", book);
        model.addAttribute("comments", result.getItems());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", totalPages);

        return "book-comments";
    }

    @PostMapping
    public String addComment(
            @PathVariable long bookId,
            @RequestParam String text,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        commentService.addComment(bookId, email, text);
        log.info("Comment added by user '{}' to book {}", email, bookId);

        return "redirect:/books/" + bookId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(params = "_method=delete")
    public String deleteComment(
            @PathVariable long bookId,
            @RequestParam long commentId,
            @RequestParam Instant createdAt) {

        commentService.delete(bookId, commentId, createdAt);
        log.info("Comment {} deleted from book {}", commentId, bookId);

        return "redirect:/books/" + bookId;
    }
}
