package com.example.MobileAppBackend.controller;

import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterRequestDto;
import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterResponseDto;
import com.example.MobileAppBackend.dto.authentication.user.LoginRequest;
import com.example.MobileAppBackend.dto.authentication.user.RegisterRequest;
import com.example.MobileAppBackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void register_shouldReturn200_whenSuccessful() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void clientRegister_shouldReturn201_andResponseBody() throws Exception {
        ClientRegisterRequestDto requestDto = new ClientRegisterRequestDto();
        requestDto.setEmail("client@example.com");
        requestDto.setUsername("clientUser");
        requestDto.setPassword("clientPass");

        ClientRegisterResponseDto responseDto = new ClientRegisterResponseDto(
                "apiKey123",
                "privateKeyABC",
                "Client account created successfully. Save your API key and secret - they won't be shown again!"
        );

        when(authService.clientRegister(any(ClientRegisterRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/auth/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey").value("apiKey123"))
                .andExpect(jsonPath("$.privateKey").value("privateKeyABC"))
                .andExpect(jsonPath("$.message").value("Client account created successfully. Save your API key and secret - they won't be shown again!"));

        verify(authService, times(1)).clientRegister(any(ClientRegisterRequestDto.class));
    }

    @Test
    void login_shouldReturn200_andToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String token = "mocked.jwt.token";
        when(authService.login(any(LoginRequest.class))).thenReturn(token);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(token));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }
}