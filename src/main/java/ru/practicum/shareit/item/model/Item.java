package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private Integer id;
    @NotBlank
    private String name;
    private String description;
    private Boolean available;
    @NotNull
    private User owner;
    private ItemRequest request;

    public Item(Integer id, String name, String desc, Boolean isAvailable, User owner) {
        this.id = id;
        this.name = name;
        this.description = desc;
        this.available = isAvailable;
        this.owner = owner;
    }

    public Item patchItem(ItemDto newItem) {
        if (!Objects.equals(newItem.getName(), null)) {
            this.name = newItem.getName();
        }
        if (!Objects.equals(newItem.getDescription(), null)) {
            this.description = newItem.getDescription();
        }
        if (!Objects.equals(newItem.getAvailable(), null)) {
            this.available = newItem.getAvailable();
        }
        return this;
    }
}
