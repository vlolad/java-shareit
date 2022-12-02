package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        User user = new User(null, "Name", "email@ya.ru");
        userRepository.save(user);
    }

    @Test
    void testAlreadyExists() {
        User user = new User(null, "Name2", "email@ya.ru");
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user));
    }
}
