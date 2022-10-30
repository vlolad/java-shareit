package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;
    @NotBlank
    private String name;
    @Email
    private String email;

    public User updateUser(User update) {
        if (update.getName() != null) {
            this.name = update.getName();
        }
        if (update.getEmail() != null) {
            this.email = update.getEmail();
        }
        return this;
    }
}
