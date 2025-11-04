package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommentMapperTest {

    @Test
    void toCommentDto_shouldMapAllFieldsCorrectly() {
        User author = User.builder()
                .id(1L)
                .name("Alice")
                .email("a@a.com")
                .build();

        Comment comment = Comment.builder()
                .id(10L)
                .text("Nice tool!")
                .author(author)
                .created(LocalDateTime.now())
                .build();

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(comment.getAuthor().getName(), dto.getAuthorName());
        assertEquals(comment.getCreated(), dto.getCreated());
    }

    @Test
    void toCommentDto_shouldReturnNullWhenCommentIsNull() {
        assertNull(CommentMapper.toCommentDto(null));
    }
}