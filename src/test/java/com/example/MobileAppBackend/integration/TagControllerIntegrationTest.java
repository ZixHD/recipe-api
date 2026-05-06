package com.example.MobileAppBackend.integration;

import com.example.MobileAppBackend.config.JwtService;
import com.example.MobileAppBackend.dto.model.TagDto;
import com.example.MobileAppBackend.model.Tag;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.model.UserType;
import com.example.MobileAppBackend.repository.TagRepository;
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
public class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        tagRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String authHeader(User user) {
        return "Bearer " + jwtService.generateToken(user.getId(), user.getUsername());
    }

    private User createUser() {
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@test.com");
        user.setPassword("pw");
        user.setUserType(UserType.USER);
        return userRepository.save(user);
    }


    @Test
    void testGetAllTags_unauthorized() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateTag_missingName_shouldFail() throws Exception {
        User user = createUser();

        TagDto dto = new TagDto();

        mockMvc.perform(post("/api/tags/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllTags_emptyList() throws Exception {
        User user = createUser();

        mockMvc.perform(get("/api/tags")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCreateTag_duplicate_shouldFail() throws Exception {
        User user = createUser();
        Tag temp = new Tag();
        temp.setName("vegan");
        tagRepository.save(temp);

        TagDto dto = new TagDto();
        dto.setName("vegan");

        mockMvc.perform(post("/api/tags/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }


    @Test
    void testGetAllTags_success() throws Exception {

        User user = createUser();

        Tag tag1 = new Tag();
        tag1.setName("vegan");
        Tag tag2 = new Tag();
        tag2.setName("dessert");

        tagRepository.saveAll(List.of(tag1, tag2));

        mockMvc.perform(get("/api/tags")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("vegan"))
                .andExpect(jsonPath("$[1].name").value("dessert"));
    }

    @Test
    void testCreateTag_success() throws Exception {

        User user = createUser();

        TagDto dto = new TagDto();
        dto.setName("healthy");

        mockMvc.perform(post("/api/tags/create")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("healthy"));
        assert tagRepository.findAll().size() == 1;
    }
}