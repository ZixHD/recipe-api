package com.example.MobileAppBackend.controller;

import com.example.MobileAppBackend.dto.create.CreatePostRequest;
import com.example.MobileAppBackend.dto.model.*;
import com.example.MobileAppBackend.model.Ingredient;
import com.example.MobileAppBackend.model.PostRecipe;
import com.example.MobileAppBackend.model.Rating;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PostControllerTest {

    @Mock
    private PostRecipeService postRecipeService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    void testGetAllPosts() throws Exception {
        PostRecipe post = new PostRecipe();
        post.setId("post1");
        when(postRecipeService.getAllPosts()).thenReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("post1"));

        verify(postRecipeService, times(1)).getAllPosts();
    }


    @Test
    void testCreatePost() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setAuthorId("user1");
        request.setText("This is a sample post text");
        request.setTitle("Delicious Pancakes");
        request.setDescription("A simple pancake recipe for breakfast");
        request.setIngredients(List.of(new IngredientDto("Flour", "100g"), new IngredientDto("Milk", "200ml")));
        request.setSteps(List.of(new StepDto(1, "Mix ingredients", ""), new StepDto(2, "Cook on pan", "")));
        request.setTags(List.of("Breakfast", "Easy"));
        request.setCuisine("American");
        request.setAllergies(List.of("Gluten"));
        request.setDifficulty("Easy");
        request.setPrep_time(15);
        request.setCalories(250);
        PostRecipe post = new PostRecipe();
        post.setId("post1");
        post.setTitle("Test Post");

        when(postRecipeService.createPost(any(CreatePostRequest.class))).thenReturn(post);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("post1"))
                .andExpect(jsonPath("$.title").value("Test Post"));

        verify(postRecipeService, times(1)).createPost(any(CreatePostRequest.class));
    }

    @Test
    void testEditPost() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setAuthorId("user1");;
        request.setText("This is a sample post text");
        request.setTitle("Delicious Pancakes");
        request.setDescription("A simple pancake recipe for breakfast");
        request.setIngredients(List.of(new IngredientDto("Flour", "100g"), new IngredientDto("Milk", "200ml")));
        request.setSteps(List.of(new StepDto(1, "Mix ingredients", ""), new StepDto(2, "Cook on pan", "")));
        request.setTags(List.of("Breakfast", "Easy"));
        request.setCuisine("American");
        request.setAllergies(List.of("Gluten"));
        request.setDifficulty("Easy");
        request.setPrep_time(15);
        request.setCalories(250);

        PostRecipe updatedPost = new PostRecipe();
        updatedPost.setId("post1");
        updatedPost.setTitle("Updated Post");

        when(postRecipeService.editPost(eq("post1"), any(CreatePostRequest.class))).thenReturn(updatedPost);

        mockMvc.perform(put("/api/posts/edit/post1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("post1"))
                .andExpect(jsonPath("$.title").value("Updated Post"));

        verify(postRecipeService, times(1)).editPost(eq("post1"), any(CreatePostRequest.class));
    }

    @Test
    void testDeletePost() throws Exception {
        doNothing().when(postRecipeService).deletePost("post1");

        mockMvc.perform(delete("/api/posts/post1"))
                .andExpect(status().isNoContent());

        verify(postRecipeService, times(1)).deletePost("post1");
    }

    @Test
    void testFilterPosts() throws Exception {
        FilterRequest request = new FilterRequest();
        request.setTags(List.of("Breakfast", "Easy"));
        PostRecipe post = new PostRecipe();
        post.setId("post1");
        post.setAuthorId("user1");
        post.setText("This is a sample post text");
        post.setTitle("Delicious Pancakes");
        post.setDescription("A simple pancake recipe for breakfast");
        post.setIngredients(List.of(new Ingredient("Flour", "100g"), new Ingredient("Milk", "200ml")));
        post.setTags(List.of("Breakfast", "Easy"));
        post.setCuisine("American");
        post.setAllergies(List.of("Gluten"));
        post.setDifficulty("Easy");
        post.setPrep_time(15);
        post.setCalories(250);
        post.setRatings(List.of(new Rating("user2", 5)));
        post.setViews(0);
        post.setCreated_at(LocalDateTime.now());

        when(postRecipeService.filterPosts(any(FilterRequest.class))).thenReturn(List.of(post));

        mockMvc.perform(post("/api/posts/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("post1"));

        verify(postRecipeService, times(1)).filterPosts(any(FilterRequest.class));
    }
}