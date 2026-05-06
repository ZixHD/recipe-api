package com.example.MobileAppBackend.integration;

import com.example.MobileAppBackend.config.JwtService;
import com.example.MobileAppBackend.dto.create.CreateUserRequest;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.model.UserType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    private String authHeader(User user) {
        return "Bearer " + jwtService.generateToken(user.getId(), user.getUsername());
    }

    @Test
    void testCreateUser_success() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("pw");
        user1.setUserType(UserType.USER);
        userRepository.save(user1);

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("newuser@example.com");
        request.setUsername("newuser");
        request.setPassword("Password123");

        String token = authHeader(user1);

        mockMvc.perform(post("/api/users/create")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        User saved = userRepository.findByUsername("newuser").orElse(null);
        assert saved != null;
        assert saved.getEmail().equals("newuser@example.com");
    }

    @Test
    void testGetAllUsers_success() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("pw");
        user1.setUserType(UserType.USER);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("pw");
        user2.setUserType(UserType.USER);

        userRepository.save(user1);
        userRepository.save(user2);

        String token = authHeader(user1);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetUserById_success() throws Exception {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        String token = authHeader(user);

        mockMvc.perform(get("/api/users/" + user.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
    }

    @Test
    void testGetUserByUsername_success() throws Exception {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        String token = authHeader(user);

        mockMvc.perform(get("/api/users/username/user1")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
    }

    @Test
    void testEditUser_success() throws Exception {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        String id = user.getId();
        String token = authHeader(user);

        CreateUserRequest editRequest = new CreateUserRequest();
        editRequest.setEmail("updated@example.com");
        editRequest.setUsername("user1");
        editRequest.setPassword("NewPassword123");

        mockMvc.perform(put("/api/users/edit/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void testDeleteUser_success() throws Exception {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        userRepository.save(user);

        String id = user.getId();
        String token = authHeader(user);

        mockMvc.perform(delete("/api/users/"+id)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        assert userRepository.findByUsername("user1").isEmpty();
    }
}
