package com.example.MobileAppBackend.integration;


import com.example.MobileAppBackend.config.JwtService;
import com.example.MobileAppBackend.dto.model.FilterRequest;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRecipeRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        postRepository.deleteAll();
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
    void testGetAllRecipes_success() throws Exception {

        User user = createUser();

        PostRecipe r1 = new PostRecipe();
        r1.setTitle("Recipe 1");
        r1.setAuthorId(user.getId());

        PostRecipe r2 = new PostRecipe();
        r2.setTitle("Recipe 2");
        r2.setAuthorId(user.getId());

        postRepository.saveAll(List.of(r1, r2));

        mockMvc.perform(get("/api/recipes")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testIncludeRecipes_success() throws Exception {

        User user = createUser();

        PostRecipe vegan = new PostRecipe();
        vegan.setTitle("Vegan Dish");
        vegan.setCuisine("vegan");
        vegan.setText("Some text");

        PostRecipe meat = new PostRecipe();
        meat.setTitle("Meat Dish");
        meat.setCuisine("meat");
        meat.setText("Other text");

        postRepository.saveAll(List.of(vegan, meat));

        mockMvc.perform(get("/api/recipes")
                        .param("include", "title,cuisine")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].cuisine").exists())
                .andExpect(jsonPath("$[0].text").doesNotExist());
    }

    @Test
    void testExcludeRecipes_success() throws Exception {

        User user = createUser();

        PostRecipe nuts = new PostRecipe();
        nuts.setTitle("Nut Recipe");
        nuts.setText("Some text");
        nuts.setCuisine("Dessert");
        nuts.setAllergies(List.of("nuts"));

        PostRecipe safe = new PostRecipe();
        safe.setTitle("Safe Recipe");
        safe.setText("Safe text");
        safe.setCuisine("Healthy");

        postRepository.saveAll(List.of(nuts, safe));

        mockMvc.perform(get("/api/recipes")
                        .param("exclude", "text,cuisine")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").doesNotExist())
                .andExpect(jsonPath("$[0].cuisine").doesNotExist())
                .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void testIncludeAndExcludeTogether_badRequest() throws Exception {

        User user = createUser();

        mockMvc.perform(get("/api/recipes")
                        .param("include", "vegan")
                        .param("exclude", "nuts")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllRecipes_emptyDb_shouldReturnEmptyList() throws Exception {
        User user = createUser();

        mockMvc.perform(get("/api/recipes")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllRecipes_noAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isForbidden());
    }


    @Test
    void testIncludeUnknownField_shouldHandleGracefully() throws Exception {
        User user = createUser();

        PostRecipe r = new PostRecipe();
        r.setTitle("Test");
        postRepository.save(r);

        mockMvc.perform(get("/api/recipes")
                        .param("include", "unknownField")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk()); // or isBadRequest() if validated
    }

    @Test
    void testExcludeAllFields_shouldReturnEmptyObjects() throws Exception {
        User user = createUser();

        PostRecipe r = new PostRecipe();
        r.setTitle("Recipe");
        r.setCuisine("Italian");
        r.setText("Text");
        postRepository.save(r);

        mockMvc.perform(get("/api/recipes")
                        .param("exclude", "title,cuisine,text")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").doesNotExist())
                .andExpect(jsonPath("$[0].cuisine").doesNotExist())
                .andExpect(jsonPath("$[0].text").doesNotExist());
    }

    @Test
    void testFilterRecipes_noMatches_shouldReturnEmptyList() throws Exception {
        User user = createUser();

        PostRecipe r = new PostRecipe();
        r.setTitle("Meat Dish");
        r.setCuisine("meat");
        postRepository.save(r);

        FilterRequest filter = new FilterRequest();
        filter.setCuisine("vegan");

        mockMvc.perform(post("/api/recipes/filter")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testFilterRecipes_nullBody_shouldFail() throws Exception {
        User user = createUser();

        mockMvc.perform(post("/api/recipes/filter")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testIncludeLargeFieldList_shouldNotCrash() throws Exception {
        User user = createUser();

        PostRecipe r = new PostRecipe();
        r.setTitle("Test");
        postRepository.save(r);

        mockMvc.perform(get("/api/recipes")
                        .param("include", "title,cuisine,text,authorId,allergies,random1,random2")
                        .header("Authorization", authHeader(user)))
                .andExpect(status().isOk());
    }

    @Test
    void testFilterCaseSensitivity() throws Exception {
        User user = createUser();

        PostRecipe r = new PostRecipe();
        r.setTitle("Vegan Dish");
        r.setCuisine("vegan");
        postRepository.save(r);

        FilterRequest filter = new FilterRequest();
        filter.setCuisine("VEGAN");

        mockMvc.perform(post("/api/recipes/filter")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk());
    }




    // izmisljam malo testove, sa edgecasevima sta vraca
    @Test
    void testFilterRecipes_success() throws Exception {

        User user = createUser();

        PostRecipe vegan = new PostRecipe();
        vegan.setTitle("Vegan Dish");
        vegan.setCuisine("vegan");

        PostRecipe meat = new PostRecipe();
        meat.setTitle("Meat Dish");
        meat.setCuisine("meat");

        postRepository.saveAll(List.of(vegan, meat));

        FilterRequest filter = new FilterRequest();
        filter.setCuisine("vegan");

        mockMvc.perform(post("/api/recipes/filter")
                        .header("Authorization", authHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Vegan Dish"))
                .andExpect(jsonPath("$[0].cuisine").value("vegan"));
    }
}
