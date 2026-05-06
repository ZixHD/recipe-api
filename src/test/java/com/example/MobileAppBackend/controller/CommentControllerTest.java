package com.example.MobileAppBackend.controller;


import com.example.MobileAppBackend.dto.create.CreateCommentRequest;
import com.example.MobileAppBackend.model.Comment;
import com.example.MobileAppBackend.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void testGetAllUserComments() throws Exception {
        Comment comment1 = new Comment();
        comment1.setText("Comment 1");
        Comment comment2 = new Comment();
        comment2.setText("Comment 2");

        List<Comment> comments = Arrays.asList(comment1, comment2);

        when(commentService.getAllUserComments("user1")).thenReturn(comments);

        mockMvc.perform(get("/api/comments/user/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Comment 1"))
                .andExpect(jsonPath("$[1].text").value("Comment 2"));

        verify(commentService, times(1)).getAllUserComments("user1");
    }

    @Test
    void testGetAllPostComments() throws Exception {
        Comment comment = new Comment();
        comment.setText("Post Comment");

        when(commentService.getAllPostComments("post1")).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/post/post1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Post Comment"));

        verify(commentService, times(1)).getAllPostComments("post1");
    }

    @Test
    void testCreateComment() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("New Comment");
        request.setAuthorId("user1");
        request.setPostId("post1");

        Comment savedComment = new Comment();
        savedComment.setText(request.getText());
        savedComment.setAuthorId(request.getAuthorId());
        savedComment.setPostId(request.getPostId());

        when(commentService.createComment(any(CreateCommentRequest.class))).thenReturn(savedComment);

        mockMvc.perform(post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("New Comment"))
                .andExpect(jsonPath("$.authorId").value("user1"))
                .andExpect(jsonPath("$.postId").value("post1"));

        verify(commentService, times(1)).createComment(any(CreateCommentRequest.class));
    }

    @Test
    void testEditComment() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("Updated Comment");
        request.setAuthorId("user1");
        request.setPostId("post1");

        Comment updatedComment = new Comment();
        updatedComment.setId("comment1");
        updatedComment.setText("Updated Comment");

        when(commentService.editComment(eq("comment1"), any(CreateCommentRequest.class)))
                .thenReturn(updatedComment);

        mockMvc.perform(put("/api/comments/edit/comment1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated Comment"));

        verify(commentService, times(1)).editComment(eq("comment1"), any(CreateCommentRequest.class));
    }

    @Test
    void testDeleteComment() throws Exception {
        doNothing().when(commentService).deleteComment("comment1");

        mockMvc.perform(delete("/api/comments/comment1"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteComment("comment1");
    }
}