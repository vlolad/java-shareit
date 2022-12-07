package ru.practicum.shareit.integrates;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

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
public class UserServiceIntegrateTests {

    private final UserService userService;

    @Test
    void testGetAllUsers() {
        UserDto user1 = userService.create(makeUserDto(1));
        UserDto user2 = userService.create(makeUserDto(2));
        UserDto user3 = userService.create(makeUserDto(3));

        List<UserDto> result = userService.getAllUsers();
        assertThat(result.size(), equalTo(3));
    }

    private UserDto makeUserDto(Integer id) {
        return new UserDto(null, "user" + id, "test" + id + "@ya.ru");
    }
}
