package com.example.MobileAppBackend.service;


import com.example.MobileAppBackend.config.ApiKeyService;
import com.example.MobileAppBackend.config.JwtService;
import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterRequestDto;
import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterResponseDto;
import com.example.MobileAppBackend.dto.authentication.user.LoginRequest;
import com.example.MobileAppBackend.dto.authentication.user.RegisterRequest;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.model.UserType;
import com.example.MobileAppBackend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;


    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@test.com");
        req.setPassword("123");

        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("hashed");

        authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("a@test.com", saved.getEmail());
        assertEquals("hashed", saved.getPassword());
        assertEquals(UserType.USER, saved.getUserType());
    }

    @Test
    void register_throws_whenEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@test.com");

        when(userRepository.findByEmail("a@test.com"))
                .thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(req));

        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@test.com");
        req.setPassword("123");

        User user = new User();
        user.setId("id1");
        user.setUsername("u");
        user.setPassword("$2a$10$hash"); // not actually used

        when(userRepository.findByEmail("a@test.com"))
                .thenReturn(Optional.of(user));

        AuthService spy = spy(authService);
        doReturn(true).when(spy).verifyPassword(any(), any());

        when(jwtService.generateToken("id1", "u"))
                .thenReturn("jwt-token");

        String token = spy.login(req);

        assertEquals("jwt-token", token);
    }

    @Test
    void login_throws_whenUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("x@test.com");

        when(userRepository.findByEmail("x@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(req));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void login_throws_whenWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@test.com");
        req.setPassword("bad");

        User user = new User();
        user.setPassword("hash");

        when(userRepository.findByEmail("a@test.com"))
                .thenReturn(Optional.of(user));

        AuthService spy = spy(authService);
        doReturn(false).when(spy).verifyPassword(any(), any());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> spy.login(req));

        assertEquals("Invalid email or password", ex.getMessage());
    }


    @Test
    void clientRegister_success() {
        ClientRegisterRequestDto req = new ClientRegisterRequestDto();
        req.setEmail("dev@test.com");
        req.setUsername("dev");
        req.setPassword("pw");

        when(userRepository.existsUserByEmail("dev@test.com")).thenReturn(false);
        when(apiKeyService.generateApiKey()).thenReturn("APIKEY123");

        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        ClientRegisterResponseDto res = authService.clientRegister(req);

        assertEquals("APIKEY123", res.getApiKey());
    }

    @Test
    void clientRegister_throws_whenEmailExists() {
        ClientRegisterRequestDto req = new ClientRegisterRequestDto();
        req.setEmail("dev@test.com");

        when(userRepository.existsUserByEmail("dev@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.clientRegister(req));

        assertEquals("Email already registered", ex.getMessage());
    }
}