package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker(User booker);
    List<Booking> findAllByItem(Item item);
    Optional<Booking> findFirstByItem_IdAndBooker_IdAndStatusAndEndBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime end);
}