package ru.practicum.shareit.request;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestClient requestClient;

    @InjectMocks
    private ItemRequestController requestController;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .build();
    }

    @Test
    void create_shouldReturnOk() throws Exception {
        when(requestClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturnBadRequestWithoutHeader() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwn_shouldReturnOk() throws Exception {
        when(requestClient.getOwn(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        when(requestClient.getAll(anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(requestClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.status(HttpStatus.OK).body(requestDto));

        mockMvc.perform(get("/requests/5")
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isOk());
    }
}