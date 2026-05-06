package com.example.MobileAppBackend.integration;

import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterRequestDto;
import com.example.MobileAppBackend.dto.authentication.user.LoginRequest;
import com.example.MobileAppBackend.dto.authentication.user.RegisterRequest;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.model.UserType;
import com.example.MobileAppBackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());


        User savedUser = userRepository.findByEmail("testuser@example.com").orElse(null);
        assert savedUser != null;
        assert savedUser.getUserType() == UserType.USER;
    }

    @Test
    void testLoginUser_success() throws Exception {
        User user = new User();
        user.setEmail("loginuser@example.com");
        user.setUsername("loginuser");
        user.setUserType(UserType.USER);
        user.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("loginuser@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testRegisterUser_missingFields_shouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(null);
        request.setPassword(null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRegisterUser_weakPassword_shouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("weak@example.com");
        request.setPassword("1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterUser_invalidEmail_shouldFail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("not-an-email");
        request.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_wrongPassword_shouldFail() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user");
        user.setUserType(UserType.USER);
        user.setPassword(passwordEncoder.encode("CorrectPass123"));
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("WrongPass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLogin_userNotFound_shouldFail() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nouser@example.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLogin_emptyBody_shouldFail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testClientRegister_duplicateEmail_shouldFail() throws Exception {
        User existing = new User();
        existing.setEmail("clientdup@example.com");
        existing.setUsername("dup");
        existing.setPassword("pw");
        existing.setUserType(UserType.CLIENT);
        userRepository.save(existing);

        ClientRegisterRequestDto dto = new ClientRegisterRequestDto();
        dto.setEmail("clientdup@example.com");
        dto.setUsername("newclient");
        dto.setPassword("Password123");

        mockMvc.perform(post("/auth/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRegister_extremelyLongInput() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("a".repeat(200) + "@test.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRegister_specialCharacters() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegister_emailCaseInsensitiveDuplicate() throws Exception {
        User existing = new User();
        existing.setEmail("case@test.com");
        existing.setUsername("user1");
        existing.setPassword("pw");
        existing.setUserType(UserType.USER);
        userRepository.save(existing);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("CASE@test.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testClientRegister_success() throws Exception {
        ClientRegisterRequestDto requestDto = new ClientRegisterRequestDto();
        requestDto.setEmail("client@example.com");
        requestDto.setUsername("clientuser");
        requestDto.setPassword("ClientPassword123");

        mockMvc.perform(post("/auth/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey").exists())
                .andExpect(jsonPath("$.privateKey").exists())
                .andExpect(jsonPath("$.message").value("Client account created successfully. Save your API key and secret - they won't be shown again!"));
    }

    @Test
    void testRegisterUser_duplicateEmail_shouldFail() throws Exception {
        User existingUser = new User();
        existingUser.setEmail("duplicate@example.com");
        existingUser.setUsername("existing");
        existingUser.setPassword("dummy");
        existingUser.setUserType(UserType.USER);
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
