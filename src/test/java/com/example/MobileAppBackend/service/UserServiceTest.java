package com.example.MobileAppBackend.service;

import com.example.MobileAppBackend.dto.create.CreateUserRequest;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private void mockAuthenticatedUser(User user) {
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext context = Mockito.mock(SecurityContext.class);

        when(auth.getPrincipal()).thenReturn(user);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_success() {
        CreateUserRequest dto = new CreateUserRequest();
        dto.setEmail("test@test.com");

        User mappedUser = new User();
        mappedUser.setEmail("test@test.com");

        when(userRepository.existsUserByEmail(dto.getEmail())).thenReturn(false);
        when(modelMapper.map(dto, User.class)).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(mappedUser);

        User result = userService.createUser(dto);

        assertEquals("test@test.com", result.getEmail());
        verify(userRepository).save(mappedUser);
    }

    @Test
    void createUser_emailExists_throwsException() {
        CreateUserRequest dto = new CreateUserRequest();
        dto.setEmail("test@test.com");

        when(userRepository.existsUserByEmail(dto.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.createUser(dto));

        assertEquals("User with this email already exists", ex.getMessage());
    }

    @Test
    void editUser_success() {
        String id = "123";

        User currentUser = new User();
        currentUser.setId(id);

        mockAuthenticatedUser(currentUser);

        User existingUser = new User();
        existingUser.setId(id);

        CreateUserRequest dto = new CreateUserRequest();
        dto.setUsername("newName");

        User mappedUser = new User();
        mappedUser.setUsername("newName");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(modelMapper.map(dto, User.class)).thenReturn(mappedUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.editUser(id, dto);

        assertEquals("newName", result.getUsername());
    }

    @Test
    void editUser_notOwner_throwsException() {
        String id = "123";

        User currentUser = new User();
        currentUser.setId("999");

        mockAuthenticatedUser(currentUser);

        User existingUser = new User();
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(modelMapper.map(any(), eq(User.class))).thenReturn(new User());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.editUser(id, new CreateUserRequest()));

        assertEquals("You are not allowed to edit this user ", ex.getMessage());
    }

    @Test
    void deleteUser_success() {
        String id = "123";

        User currentUser = new User();
        currentUser.setId(id);

        mockAuthenticatedUser(currentUser);

        User existingUser = new User();
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(id);

        verify(userRepository).delete(existingUser);
    }

    @Test
    void deleteUser_notOwner_throwsException() {
        String id = "123";

        User currentUser = new User();
        currentUser.setId("999");

        mockAuthenticatedUser(currentUser);

        User existingUser = new User();
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(id));

        assertEquals("You are not allowed to remove this user ", ex.getMessage());
    }

}
