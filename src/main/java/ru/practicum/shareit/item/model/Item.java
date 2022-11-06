package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;

    public Item(Integer id, String name, String desc, Boolean isAvailable, User owner) {
        this.id = id;
        this.name = name;
        this.description = desc;
        this.available = isAvailable;
        this.owner = owner;
    }
}
