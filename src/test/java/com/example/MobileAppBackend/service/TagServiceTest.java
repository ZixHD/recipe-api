package com.example.MobileAppBackend.service;

import com.example.MobileAppBackend.dto.model.TagDto;
import com.example.MobileAppBackend.model.Tag;
import com.example.MobileAppBackend.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TagService tagService;


    @Test
    void findAllTags_returnsAllTags() {
        Tag t1 = new Tag();
        Tag t2 = new Tag();

        when(tagRepository.findAll()).thenReturn(List.of(t1, t2));

        List<Tag> result = tagService.findAllTags();

        assertEquals(2, result.size());
        verify(tagRepository).findAll();
    }


    @Test
    void createTag_createsTag_whenNotExists() {
        TagDto dto = new TagDto();
        dto.setName("Vegan");

        Tag mappedTag = new Tag();
        mappedTag.setName("Vegan");

        when(tagRepository.existsTagByName("Vegan")).thenReturn(false);
        when(modelMapper.map(dto, Tag.class)).thenReturn(mappedTag);
        when(tagRepository.save(mappedTag)).thenReturn(mappedTag);

        Tag result = tagService.createTag(dto);

        assertNotNull(result);
        assertEquals("Vegan", result.getName());

        verify(tagRepository).existsTagByName("Vegan");
        verify(modelMapper).map(dto, Tag.class);
        verify(tagRepository).save(mappedTag);
    }


    @Test
    void createTag_throwsException_whenTagExists() {
        TagDto dto = new TagDto();
        dto.setName("Vegan");

        when(tagRepository.existsTagByName("Vegan")).thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> tagService.createTag(dto)
        );

        assertEquals("Tag with this name already exists", ex.getMessage());

        verify(tagRepository).existsTagByName("Vegan");
        verify(tagRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }
}