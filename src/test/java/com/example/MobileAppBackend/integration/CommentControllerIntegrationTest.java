package com.example.MobileAppBackend.integration;

import com.example.MobileAppBackend.config.JwtService;
import com.example.MobileAppBackend.dto.create.CreateCommentRequest;
import com.example.MobileAppBackend.model.Comment;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.model.UserType;
import com.example.MobileAppBackend.repository.CommentRepository;
import com.example.MobileAppBackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        commentRepository.deleteAll();
        userRepository.deleteAll();
    }
    private User createUser() {
        User user = new User();
        user.setUsername("user" + System.nanoTime());
        user.setEmail("u" + System.nanoTime() + "@test.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        return userRepository.save(user);
    }

    private String authHeader(User user) {
        return "Bearer " + jwtService.generateToken(user.getId(), user.getUsername());
    }

    @Test
    void testCreateComment_missingText_shouldFail() throws Exception {
        User user = createUser();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthorId(user.getId());
        request.setPostId("post123");
        request.setText(null);

        mockMvc.perform(post("/api/comments/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateComment_noAuth_shouldReturnError() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthorId("x");
        request.setPostId("p");
        request.setText("hello");

        mockMvc.perform(post("/api/comments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testEditComment_notFound_shouldReturnError() throws Exception {
        User user = createUser();

        CreateCommentRequest editRequest = new CreateCommentRequest();
        editRequest.setAuthorId(user.getId());
        editRequest.setPostId("post1");
        editRequest.setText("Updated");

        mockMvc.perform(put("/api/comments/edit/nonexistent")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testDeleteComment_notFound_shouldReturnError() throws Exception {
        User user = createUser();

        mockMvc.perform(delete("/api/comments/nonexistent")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetUserComments_empty_shouldReturnEmptyList() throws Exception {
        User user = createUser();

        mockMvc.perform(get("/api/comments/user/" + user.getId())
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateComment_veryLongText_lessThanLimit() throws Exception {
        User user = createUser();

        String longText = "a".repeat(100);

        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthorId(user.getId());
        request.setPostId("post123");
        request.setText(longText);

        mockMvc.perform(post("/api/comments/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateComment_xssInput() throws Exception {
        User user = createUser();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthorId(user.getId());
        request.setPostId("post123");
        request.setText("<script>alert('xss')</script>");

        mockMvc.perform(post("/api/comments/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateComment_authorSpoofing_shouldFail() throws Exception {
        User realUser = createUser();
        User otherUser = createUser();

        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthorId(otherUser.getId());
        request.setPostId("post123");
        request.setText("Spoofed");

        mockMvc.perform(post("/api/comments/create")
                        .header("Authorization", authHeader(realUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateComment_emptyBody_shouldFail() throws Exception {
        User user = createUser();

        mockMvc.perform(post("/api/comments/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateComment_success() throws Exception {

        User user = new User();
        user.setUsername("author");
        user.setEmail("author@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthorId(user.getId());
        request.setPostId("post123");
        request.setText("This is a comment");

        mockMvc.perform(post("/api/comments/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("This is a comment"))
                .andExpect(jsonPath("$.authorId").value(user.getId()))
                .andExpect(jsonPath("$.postId").value("post123"));

        List<Comment> saved = commentRepository.findAll();
        assert saved.size() == 1;
        assert saved.get(0).getText().equals("This is a comment");
    }

    @Test
    void testGetAllUserComments_success() throws Exception {
        User user = new User();
        user.setUsername("author");
        user.setEmail("author@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        Comment comment1 = new Comment();
        comment1.setPostId("post1");
        comment1.setAuthorId(user.getId());
        comment1.setText("Comment 1");

        Comment comment2 = new Comment();
        comment2.setPostId("post1");
        comment2.setAuthorId(user.getId());
        comment2.setText("Comment 1");
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        mockMvc.perform(get("/api/comments/user/" + user.getId())
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetAllPostComments_success() throws Exception {
        User user = new User();
        user.setUsername("author");
        user.setEmail("author@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        Comment comment1 = new Comment();
        comment1.setPostId("post1");
        comment1.setAuthorId(user.getId());
        comment1.setText("Comment 1");

        Comment comment2 = new Comment();
        comment2.setPostId("post2");
        comment2.setAuthorId(user.getId());
        comment2.setText("Comment 2");

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        mockMvc.perform(get("/api/comments/post/post1")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }



    @Test
    void testEditComment_success() throws Exception {
        User user = new User();
        user.setUsername("author");
        user.setEmail("author@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        Comment comment1 = new Comment();
        comment1.setPostId("post1");
        comment1.setAuthorId(user.getId());
        comment1.setText("Comment 1");
        commentRepository.save(comment1);

        CreateCommentRequest editRequest = new CreateCommentRequest();
        editRequest.setAuthorId(user.getId());
        editRequest.setPostId("post1");
        editRequest.setText("Updated content");

        mockMvc.perform(put("/api/comments/edit/" + comment1.getId())
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated content"));
    }

    @Test
    void testDeleteComment_success() throws Exception {
        User user = new User();
        user.setUsername("author");
        user.setEmail("author@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        Comment comment1 = new Comment();
        comment1.setPostId("post1");
        comment1.setAuthorId(user.getId());
        comment1.setText("Comment 1");
        commentRepository.save(comment1);

        mockMvc.perform(delete("/api/comments/" + comment1.getId())
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk());

        assert commentRepository.findById(comment1.getId()).isEmpty();
    }
}
