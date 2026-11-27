package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateDto {
    @NotNull(message = "Не указана вещь")
    private Long itemId;

    @NotNull(message = "Не указана дата начала")
    @FutureOrPresent(message = "Начало не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Не указана дата окончания")
    @Future(message = "Окончание должно быть в будущем")
    private LocalDateTime end;
}
