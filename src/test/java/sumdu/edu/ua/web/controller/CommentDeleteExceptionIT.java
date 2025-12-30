package sumdu.edu.ua.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.AppInit;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AppInit.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentDeleteExceptionIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BookService bookService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteComment_whenCommentTooOld_returnsBadRequest() throws Exception {
        doThrow(new CommentTooOldException("Comment was created more than 24 hours ago and cannot be deleted"))
                .when(commentService).delete(eq(1L), eq(2L), any(Instant.class));

        mockMvc.perform(delete("/comments")
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Comment was created more than 24 hours ago and cannot be deleted"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteComment_withInvalidBookId_returnsBadRequest() throws Exception {
        doThrow(new InvalidCommentDeleteException("bookId must be greater than 0"))
                .when(commentService).delete(eq(0L), eq(2L), any(Instant.class));

        mockMvc.perform(delete("/comments")
                        .param("bookId", "0")
                        .param("commentId", "2")
                        .param("createdAt", "2024-12-27T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bookId must be greater than 0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteComment_withInvalidCommentId_returnsBadRequest() throws Exception {
        doThrow(new InvalidCommentDeleteException("commentId must be greater than 0"))
                .when(commentService).delete(eq(1L), eq(-1L), any(Instant.class));

        mockMvc.perform(delete("/comments")
                        .param("bookId", "1")
                        .param("commentId", "-1")
                        .param("createdAt", "2024-12-27T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("commentId must be greater than 0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteComment_withValidData_returnsOk() throws Exception {
        mockMvc.perform(delete("/comments")
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", "2024-12-27T10:00:00Z"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(delete("/comments")
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", "2024-12-27T10:00:00Z"))
                .andExpect(status().is3xxRedirection());
    }

    // ========== Tests for InvalidCommentTextException ==========

    @Test
    @WithMockUser(roles = "USER")
    void addComment_withForbiddenWord_returnsBadRequest() throws Exception {
        when(bookService.findById(anyLong())).thenReturn(new Book(1L, "Test Book", "Test Author", 2024));
        doThrow(new InvalidCommentTextException("Comment contains forbidden word: spam"))
                .when(commentService).addComment(anyLong(), anyString(), anyString());

        mockMvc.perform(post("/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"bookId\": 1, \"author\": \"user@test.com\", \"text\": \"This is spam!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Comment contains forbidden word: spam"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addComment_withTextTooLong_returnsBadRequest() throws Exception {
        when(bookService.findById(anyLong())).thenReturn(new Book(1L, "Test Book", "Test Author", 2024));
        doThrow(new InvalidCommentTextException("Comment text exceeds maximum length of 1000 characters"))
                .when(commentService).addComment(anyLong(), anyString(), anyString());

        mockMvc.perform(post("/comments")
                        .contentType(APPLICATION_JSON)
                        .content("{\"bookId\": 1, \"author\": \"user@test.com\", \"text\": \"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Comment text exceeds maximum length of 1000 characters"));
    }
}

