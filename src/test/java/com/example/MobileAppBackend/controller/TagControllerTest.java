package com.example.MobileAppBackend.controller;

import com.example.MobileAppBackend.dto.model.TagDto;
import com.example.MobileAppBackend.model.Tag;
import com.example.MobileAppBackend.service.TagService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tagController).build();
    }

    @Test
    void getAllTags() throws Exception {
        Tag tag = new Tag();
        tag.setId("tag1");
        tag.setName("Breakfast");

        when(tagService.findAllTags()).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tag1"))
                .andExpect(jsonPath("$[0].name").value("Breakfast"));

        verify(tagService).findAllTags();
    }

    @Test
    void createTag() throws Exception {
        TagDto dto = new TagDto();
        dto.setName("Vegan");

        Tag saved = new Tag();
        saved.setId("tag1");
        saved.setName("Vegan");

        when(tagService.createTag(any(TagDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/tags/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tag1"))
                .andExpect(jsonPath("$.name").value("Vegan"));

        verify(tagService).createTag(any(TagDto.class));
    }
}