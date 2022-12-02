package ru.practicum.shareit.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Item item = new Item();
        item.setAvailable(true);
        item.setDescription("Test_desc");
        item.setName("Bob's brush");
        item.setOwner(null);
        item.setRequestId(123);
        itemRepository.save(item);
        Item item1 = new Item();
        item1.setAvailable(true);
        item1.setDescription("Test_desc2");
        item1.setName("Chair brush");
        item1.setOwner(null);
        item1.setRequestId(123);
        itemRepository.save(item1);
    }

    @Test
    void testSearch() {
        Pageable page = PageRequest.of(0, 20);
        List<Item> result = itemRepository.search("Chair", page).getContent();

        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getName(), equalTo("Chair brush"));
    }
}

