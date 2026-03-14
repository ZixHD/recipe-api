package com.example.MobileAppBackend.service;

import com.example.MobileAppBackend.dto.create.CreateCommentRequest;
import com.example.MobileAppBackend.model.Comment;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.repository.CommentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CommentService commentService;


    private void mockAuthenticatedUser(String userId) {
        User user = new User();
        user.setId(userId);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void createComment_success() {
        mockAuthenticatedUser("u1");
        CreateCommentRequest req = new CreateCommentRequest();
        req.setText("hello");
        req.setAuthorId("u1");

        Comment mapped = new Comment();
        mapped.setText("hello");

        when(modelMapper.map(req, Comment.class)).thenReturn(mapped);
        when(commentRepository.save(mapped)).thenReturn(mapped);

        Comment result = commentService.createComment(req);

        assertEquals("hello", result.getText());
        verify(commentRepository).save(mapped);
    }


    @Test
    void getAllUserComments_returnsList() {
        List<Comment> list = List.of(new Comment(), new Comment());

        when(commentRepository.findCommentsByAuthorId("u1")).thenReturn(list);

        List<Comment> result = commentService.getAllUserComments("u1");

        assertEquals(2, result.size());
    }

    @Test
    void getAllPostComments_returnsList() {
        List<Comment> list = List.of(new Comment());

        when(commentRepository.findCommentsByPostId("p1")).thenReturn(list);

        List<Comment> result = commentService.getAllPostComments("p1");

        assertEquals(1, result.size());
    }

    @Test
    void editComment_success_whenOwner() {
        mockAuthenticatedUser("u1");

        Comment existing = new Comment();
        existing.setId("c1");
        existing.setAuthorId("u1");
        existing.setText("old");

        CreateCommentRequest req = new CreateCommentRequest();
        req.setText("new");

        Comment mapped = new Comment();
        mapped.setText("new");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));
        when(modelMapper.map(req, Comment.class)).thenReturn(mapped);
        when(commentRepository.save(existing)).thenReturn(existing);

        Comment result = commentService.editComment("c1", req);

        assertEquals("new", result.getText());
        verify(commentRepository).save(existing);
    }

    @Test
    void editComment_throws_whenNotOwner() {
        mockAuthenticatedUser("u2");

        Comment existing = new Comment();
        existing.setId("c1");
        existing.setAuthorId("u1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.editComment("c1", new CreateCommentRequest()));

        assertEquals("You are not allowed to edit this comment", ex.getMessage());
    }

    @Test
    void deleteComment_success_whenOwner() {
        mockAuthenticatedUser("u1");

        Comment existing = new Comment();
        existing.setId("c1");
        existing.setAuthorId("u1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));

        commentService.deleteComment("c1");

        verify(commentRepository).delete(existing);
    }

    @Test
    void deleteComment_throws_whenNotOwner() {
        mockAuthenticatedUser("u2");

        Comment existing = new Comment();
        existing.setId("c1");
        existing.setAuthorId("u1");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.deleteComment("c1"));

        assertEquals("You are not allowed to remove this comment", ex.getMessage());
        verify(commentRepository, never()).delete(any());
    }
}