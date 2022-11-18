package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validate.Create;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @Null(groups = {Create.class})
    private Integer id;
    @NotBlank(groups = {Create.class})
    private String name;
    @NotNull(groups = {Create.class}) @Email
    private String email;
}
