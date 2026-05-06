package com.example.MobileAppBackend.controller;

import com.example.MobileAppBackend.dto.create.CreateUserRequest;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getAllUsers() throws Exception {
        User user = new User();

        user.setUsername("john");

        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));

        verify(userService).findAll();
    }

    @Test
    void getUserById() throws Exception {
        User user = new User();
        user.setId("1");

        when(userService.getUserById("1")).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(userService).getUserById("1");
    }

    @Test
    void getUserByUsername() throws Exception {
        User user = new User();
        user.setUsername("john");

        when(userService.getUserByUsername("john")).thenReturn(user);

        mockMvc.perform(get("/api/users/username/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(userService).getUserByUsername("john");
    }

    @Test
    void createUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");
        request.setEmail("1@gmail.com");
        request.setPassword("123456");

        User saved = new User();
        saved.setUsername("john");
        saved.setEmail("1@gmail.com");
        saved.setPassword("123456");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void editUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");

        User updated = new User();
        updated.setUsername("john");

        when(userService.editUser(eq("john"), any(CreateUserRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/users/edit/john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(userService).editUser(eq("john"), any(CreateUserRequest.class));
    }

    @Test
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser("john");

        mockMvc.perform(delete("/api/users/john"))
                .andExpect(status().isOk());

        verify(userService).deleteUser("john");
    }
}