package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {

    @Null
    private Integer id;
    @NotBlank
    private String text;
    private Integer itemId;
    private String authorName;
    private LocalDateTime created;
}
