package com.example.MobileAppBackend.service;

import com.example.MobileAppBackend.dto.create.CreatePostRequest;
import com.example.MobileAppBackend.dto.model.FilterRequest;
import com.example.MobileAppBackend.dto.model.RecipeDto;
import com.example.MobileAppBackend.model.*;
import com.example.MobileAppBackend.repository.PostRecipeRepository;
import com.example.MobileAppBackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PostRecipeServiceTest {

    @Mock
    private PostRecipeRepository postRecipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PostRecipeService postRecipeService;



    private void mockAuthenticatedUser(String userId) {
        User user = new User();
        user.setId(userId);

        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        when(auth.getPrincipal()).thenReturn(user);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void getById_returnsPost_whenExists() {
        PostRecipe post = new PostRecipe();
        post.setId("p1");
        when(postRecipeRepository.findById("p1")).thenReturn(Optional.of(post));
        when(postRecipeRepository.findPostById("p1")).thenReturn(post);

        PostRecipe result = postRecipeService.getById("p1");

        assertNotNull(result);
        assertEquals("p1", result.getId());
    }

    @Test
    void getById_throws_whenNotFound() {
        when(postRecipeRepository.findById("p1")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> postRecipeService.getById("p1"));
    }

    @Test
    void getAllRecipes_mapsToDto() {
        PostRecipe post = new PostRecipe();
        post.setTitle("Test Recipe");
        when(postRecipeRepository.findAll()).thenReturn(List.of(post));

        List<RecipeDto> result = postRecipeService.getAllRecipes();

        assertEquals(1, result.size());
        assertEquals("Test Recipe", result.get(0).getTitle());
    }

    @Test
    void filterRecipes_returnsDtoList() {
        FilterRequest filter = new FilterRequest();
        PostRecipe post = new PostRecipe();
        post.setTitle("Filtered Recipe");

        // mock filterPosts to return a list
        PostRecipeService spyService = spy(postRecipeService);
        doReturn(List.of(post)).when(spyService).filterPosts(filter);

        List<RecipeDto> result = spyService.filterRecipes(filter);

        assertEquals(1, result.size());
        assertEquals("Filtered Recipe", result.get(0).getTitle());
    }

    @Test
    void editPost_updatesFields_whenAuthorMatches() {
        mockAuthenticatedUser("u1");

        PostRecipe existing = new PostRecipe();
        existing.setAuthorId("u1");

        CreatePostRequest req = new CreatePostRequest();
        req.setText("Updated text");

        when(postRecipeRepository.findById("p1")).thenReturn(Optional.of(existing));
        when(postRecipeRepository.save(existing)).thenReturn(existing);

        PostRecipe updated = postRecipeService.editPost("p1", req);

        assertEquals("Updated text", updated.getText());
    }

    @Test
    void editPost_throws_whenNotAuthor() {
        mockAuthenticatedUser("u1");

        PostRecipe existing = new PostRecipe();
        existing.setAuthorId("u2");

        CreatePostRequest req = new CreatePostRequest();

        when(postRecipeRepository.findById("p1")).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class,
                () -> postRecipeService.editPost("p1", req));
    }

    @Test
    void getSpecificRecipesInclude_returnsIncludedFieldsOnly() {
        PostRecipe post = new PostRecipe();
        post.setTitle("Recipe1");
        post.setDescription("Desc1");

        when(postRecipeRepository.findAll()).thenReturn(List.of(post));

        List<Map<String, Object>> result = postRecipeService.getSpecificRecipesInclude("title");

        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("title"));
        assertFalse(result.get(0).containsKey("description"));
    }

    @Test
    void getSpecificRecipesExclude_removesFields() {
        PostRecipe post = new PostRecipe();
        post.setTitle("Recipe1");
        post.setDescription("Desc1");

        when(postRecipeRepository.findAll()).thenReturn(List.of(post));

        List<Map<String, Object>> result = postRecipeService.getSpecificRecipesExclude("description");

        assertEquals(1, result.size());
        assertTrue(result.get(0).containsKey("title"));
        assertFalse(result.get(0).containsKey("description"));
    }




    @Test
    void toggleFavorite_addsFavorite_whenNotPresent() {
        mockAuthenticatedUser("u1");

        User user = new User();
        user.setId("u1");
        user.setFavorites(new HashSet<>());

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(postRecipeRepository.findById("p1")).thenReturn(Optional.of(new PostRecipe()));

        postRecipeService.toggleFavorite("p1");

        assertTrue(user.getFavorites().contains("p1"));
        verify(userRepository).save(user);
    }

    @Test
    void deletePost_deletes_whenAuthorMatches() {
        mockAuthenticatedUser("u1");

        PostRecipe post = new PostRecipe();
        post.setAuthorId("u1");

        when(postRecipeRepository.findById("p1")).thenReturn(Optional.of(post));

        postRecipeService.deletePost("p1");

        verify(postRecipeRepository).delete(post);
    }

    @Test
    void deletePost_throws_whenNotAuthor() {
        mockAuthenticatedUser("u1");

        PostRecipe post = new PostRecipe();
        post.setAuthorId("u2");

        when(postRecipeRepository.findById("p1")).thenReturn(Optional.of(post));

        assertThrows(RuntimeException.class,
                () -> postRecipeService.deletePost("p1"));
    }
    

    @Test
    void filterPosts_usesMongoTemplate() {
        FilterRequest filter = new FilterRequest();

        when(mongoTemplate.find(any(), eq(PostRecipe.class)))
                .thenReturn(List.of(new PostRecipe()));

        List<PostRecipe> result = postRecipeService.filterPosts(filter);

        assertEquals(1, result.size());
        verify(mongoTemplate).find(any(), eq(PostRecipe.class));
    }
}