package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name="description")
    private String desc;
    @ManyToOne
    @JoinColumn(name="requester_id", referencedColumnName="id")
    private User requester;
    @Column(name="creation_date")
    private LocalDateTime created;
}
