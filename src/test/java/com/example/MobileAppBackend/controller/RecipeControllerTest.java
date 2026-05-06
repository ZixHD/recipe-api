package com.example.MobileAppBackend.controller;

import com.example.MobileAppBackend.dto.model.FilterRequest;
import com.example.MobileAppBackend.dto.model.RecipeDto;
import com.example.MobileAppBackend.service.PostRecipeService;
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
class RecipeControllerTest {

    @Mock
    private PostRecipeService postRecipeService;

    @InjectMocks
    private RecipeController recipeController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController).build();
    }

    @Test
    void getAllRecipes_noParams() throws Exception {
        when(postRecipeService.getAllRecipes()).thenReturn(List.of());

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(postRecipeService).getAllRecipes();
    }

    @Test
    void getAllRecipes_withExclude() throws Exception {
        when(postRecipeService.getSpecificRecipesExclude("nuts"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/recipes")
                        .param("exclude", "nuts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(postRecipeService).getSpecificRecipesExclude("nuts");
    }

    @Test
    void getAllRecipes_withInclude() throws Exception {
        when(postRecipeService.getSpecificRecipesInclude("chicken"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/recipes")
                        .param("include", "chicken"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(postRecipeService).getSpecificRecipesInclude("chicken");
    }

    @Test
    void getAllRecipes_withIncludeAndExclude_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/recipes")
                        .param("include", "chicken")
                        .param("exclude", "nuts"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Only one parameter can be used: either 'include' or 'exclude', not both."
                ));

        verifyNoInteractions(postRecipeService);
    }

    @Test
    void filterRecipes() throws Exception {
        FilterRequest request = new FilterRequest();
        List<RecipeDto> response = List.of();

        when(postRecipeService.filterRecipes(any(FilterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/recipes/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(postRecipeService).filterRecipes(any(FilterRequest.class));
    }
}