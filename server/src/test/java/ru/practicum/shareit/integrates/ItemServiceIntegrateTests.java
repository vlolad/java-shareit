package ru.practicum.shareit.integrates;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = {
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.url=jdbc:h2:mem:shareit;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=test",
                "spring.datasource.password=test",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Disabled
public class ItemServiceIntegrateTests {

    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void testGetUserItems() {
        UserDto owner = userService.create(makeUserDto(1));
        UserDto owner2 = userService.create(makeUserDto(49));

        ItemDto item1 = itemService.create(makeRequest(1), owner.getId());
        ItemDto item2 = itemService.create(makeRequest(2), owner.getId());
        ItemDto item3 = itemService.create(makeRequest(3), owner2.getId());

        List<ItemDto> result = itemService.getAllByOwner(owner.getId(), 0, 20);
        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0).getName(), equalTo(item1.getName()));
        assertThat(result.get(1).getName(), equalTo(item2.getName()));

        List<ItemDto> result2 = itemService.getAllByOwner(owner2.getId(), 0, 20);
        assertThat(result2.size(), equalTo(1));
        assertThat(result2.get(0).getName(), equalTo(item3.getName()));
    }

    private CreateItemRequest makeRequest(Integer id) {
        return new CreateItemRequest(id, "test" + id, "test_desc", Boolean.TRUE, null);
    }

    private UserDto makeUserDto(Integer id) {
        return new UserDto(null, "user" + id, "test" + id + "@ya.ru");
    }
}
