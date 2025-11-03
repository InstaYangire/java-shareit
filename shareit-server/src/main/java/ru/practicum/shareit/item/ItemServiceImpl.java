package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner not found with id: " + ownerId));

        Item item = ItemMapper.toItem(itemDto, owner);

        if (itemDto.getRequest() != null && itemDto.getRequest().getId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequest().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Request not found with id: " + itemDto.getRequest().getId()));
            item.setRequest(request);
        } else if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            "Request not found with id: " + itemDto.getRequestId()));
            item.setRequest(request);
        }

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Transactional
    @Override
    public ItemDto update(Long itemId, Long ownerId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found"));
        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only owner can update item");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) existing.setName(itemDto.getName());
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank())
            existing.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) existing.setAvailable(itemDto.getAvailable());
        return ItemMapper.toItemDto(itemRepository.save(existing));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found"));

        var comments = commentRepository.findAllByItem(item).stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        ItemDto dto = ItemMapper.toItemDtoWithComments(item, comments);

        if (item.getOwner().getId().equals(userId)) {
            var bookings = bookingRepository.findAllByItem(item);
            var now = LocalDateTime.now();

            var last = bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getEnd().isBefore(now))
                    .max((b1, b2) -> b1.getEnd().compareTo(b2.getEnd()))
                    .map(BookingMapper::toShortDto)
                    .orElse(null);

            var next = bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getStart().isAfter(now))
                    .min((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                    .map(BookingMapper::toShortDto)
                    .orElse(null);

            dto.setLastBooking(last);
            dto.setNextBooking(next);
        }

        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner not found with id: " + ownerId));

        return itemRepository.findAllByOwner(owner).stream()
                .map(item -> {
                    var comments = commentRepository.findAllByItem(item).stream()
                            .map(CommentMapper::toCommentDto)
                            .toList();

                    var dto = ItemMapper.toItemDtoWithComments(item, comments);

                    var bookings = bookingRepository.findAllByItem(item);
                    var now = LocalDateTime.now();

                    var last = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getEnd().isBefore(now))
                            .max((b1, b2) -> b1.getEnd().compareTo(b2.getEnd()))
                            .map(BookingMapper::toShortDto)
                            .orElse(null);

                    var next = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getStart().isAfter(now))
                            .min((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                            .map(BookingMapper::toShortDto)
                            .orElse(null);

                    dto.setLastBooking(last);
                    dto.setNextBooking(next);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset offset = systemZone.getRules().getOffset(Instant.now());

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedBooking = bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId, BookingStatus.APPROVED, now);

        if (!hasCompletedBooking) {
            throw new ValidationException("User has not completed booking of this item");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(now)
                .build();

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}