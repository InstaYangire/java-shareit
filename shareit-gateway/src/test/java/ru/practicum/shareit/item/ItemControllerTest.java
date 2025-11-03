package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemClient itemClient;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private ItemDto itemDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .build();

        commentDto = CommentDto.builder()
                .text("Great tool!")
                .build();
    }

    @Test
    void create_shouldReturnOk() throws Exception {
        when(itemClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok().body(itemDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturnBadRequestWithoutHeader() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        when(itemClient.update(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(itemClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(itemDto));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk());
    }

    @Test
    void getByOwner_shouldReturnOk() throws Exception {
        when(itemClient.getByOwner(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk());
    }

    @Test
    void search_shouldReturnOk() throws Exception {
        when(itemClient.search(any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldReturnOk() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(commentDto));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }
}