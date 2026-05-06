package com.example.MobileAppBackend.integration;

import com.example.MobileAppBackend.config.JwtService;
import com.example.MobileAppBackend.dto.create.CreatePostRequest;
import com.example.MobileAppBackend.dto.model.IngredientDto;
import com.example.MobileAppBackend.dto.model.RatingDto;
import com.example.MobileAppBackend.dto.model.StepDto;
import com.example.MobileAppBackend.model.PostRecipe;
import com.example.MobileAppBackend.model.User;
import com.example.MobileAppBackend.model.UserType;
import com.example.MobileAppBackend.repository.PostRecipeRepository;
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


import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRecipeRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    void cleanup() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String authHeader(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return "Bearer " + token;
    }

    @Test
    void testCreatePost_success() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setEmail("author@example.com");
        author.setPassword("pw");
        author.setUserType(UserType.USER);
        userRepository.save(author);

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Delicious Cake");
        request.setAuthorId(author.getId());
        request.setDescription("This is a simple cake recipe.");
        request.setDifficulty("Easy");
        request.setCuisine("Dessert");
        request.setTags(Arrays.asList("sweet", "cake"));
        request.setAllergies(Arrays.asList("nuts"));
        request.setText("123123123");

        request.setIngredients(Arrays.asList(
                new IngredientDto("Flour", "200g"),
                new IngredientDto("Sugar", "100g"),
                new IngredientDto("Eggs", "3")
        ));

        request.setSteps(Arrays.asList(
                new StepDto(1, "Mix all ingredients", null),
                new StepDto(2, "Bake at 180°C for 30 minutes", null)
        ));


        mockMvc.perform(post("/api/posts/create")
                        .header("Authorization", authHeader(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Delicious Cake"))
                .andExpect(jsonPath("$.authorId").value(author.getId()));
    }

    @Test
    void testGetAllPosts_success() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setEmail("author@example.com");
        author.setPassword("pw");
        author.setUserType(UserType.USER);
        userRepository.save(author);

        PostRecipe post1 = new PostRecipe();
        post1.setTitle("Post 1");
        post1.setText("Content 1");
        post1.setAuthorId(author.getId());

        PostRecipe post2 = new PostRecipe();
        post2.setTitle("Post 2");
        post2.setText("Content 2");
        post2.setAuthorId(author.getId());

        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get("/api/posts")
                        .header("Authorization", authHeader(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetPostById_success() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setEmail("author@example.com");
        author.setPassword("pw");
        author.setUserType(UserType.USER);
        userRepository.save(author);

        PostRecipe post = new PostRecipe();
        post.setTitle("Single Post");
        post.setText("Single content");
        post.setAuthorId(author.getId());
        postRepository.save(post);

        mockMvc.perform(get("/api/posts/" + post.getId())
                        .header("Authorization", authHeader(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Single Post"))
                .andExpect(jsonPath("$.text").value("Single content"));
    }

    @Test
    void testEditPost_success() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setEmail("author@example.com");
        author.setPassword("pw");
        author.setUserType(UserType.USER);
        userRepository.save(author);

        PostRecipe post = new PostRecipe();
        post.setTitle("Old Title");
        post.setText("Old content");
        post.setAuthorId(author.getId());
        postRepository.save(post);

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Title");
        request.setText("New content");

        request.setAuthorId(author.getId());
        request.setDescription("This is a simple cake recipe.");
        request.setDifficulty("Easy");
        request.setCuisine("Dessert");
        request.setTags(Arrays.asList("sweet", "cake"));
        request.setAllergies(Arrays.asList("nuts"));


        request.setIngredients(Arrays.asList(
                new IngredientDto("Flour", "200g"),
                new IngredientDto("Sugar", "100g"),
                new IngredientDto("Eggs", "3")
        ));

        request.setSteps(Arrays.asList(
                new StepDto(1, "Mix all ingredients", null),
                new StepDto(2, "Bake at 180°C for 30 minutes", null)
        ));


        mockMvc.perform(put("/api/posts/edit/" + post.getId())
                        .header("Authorization", authHeader(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.text").value("New content"));
    }

    @Test
    void testDeletePost_success() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setEmail("author@example.com");
        author.setPassword("pw");
        author.setUserType(UserType.USER);
        userRepository.save(author);

        PostRecipe post = new PostRecipe();
        post.setTitle("To be deleted");
        post.setText("Will be deleted");
        post.setAuthorId(author.getId());
        postRepository.save(post);

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .header("Authorization", authHeader(author)))
                .andExpect(status().isNoContent());

        assert postRepository.findById(post.getId()).isEmpty();
    }

    @Test
    void testFavoritePost_success() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setEmail("author@example.com");
        author.setPassword("pw");
        author.setUserType(UserType.USER);
        userRepository.save(author);

        PostRecipe post = new PostRecipe();
        post.setTitle("Favorite me");
        post.setText("Content");
        post.setAuthorId(author.getId());
        postRepository.save(post);

        mockMvc.perform(put("/api/posts/favorite/" + post.getId())
                        .header("Authorization", authHeader(author)))
                .andExpect(status().isOk());
    }
}