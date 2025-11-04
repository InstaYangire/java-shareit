package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Hammer")
                .description("Steel hammer")
                .available(true)
                .build();

        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(dto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Hammer")))
                .andExpect(jsonPath("$.description", is("Steel hammer")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Hammer Updated")
                .description("Steel hammer with rubber handle")
                .available(true)
                .build();

        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(dto);

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Hammer Updated")));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .build();

        when(itemService.getById(anyLong(), anyLong())).thenReturn(dto);

        mockMvc.perform(get("/items/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Cordless drill")));
    }

    @Test
    void getByOwner_shouldReturnListOfItems() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Hammer").available(true).build();
        when(itemService.getByOwner(anyLong())).thenReturn(List.of(dto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Hammer")));
    }

    @Test
    void search_shouldReturnItems() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Saw").available(true).build();
        when(itemService.search(anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/items/search")
                        .param("text", "saw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Saw")));
    }

    @Test
    void addComment_shouldReturnSavedComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great tool")
                .authorName("John")
                .build();

        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great tool")))
                .andExpect(jsonPath("$.authorName", is("John")));
    }
}