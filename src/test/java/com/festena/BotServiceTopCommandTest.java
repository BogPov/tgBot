package com.festena;

import com.festena.databases.PlayersResDB;
import com.festena.manager.UserSessionManager;
import com.festena.service.BotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
class BotServiceTopCommandTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDbProps(DynamicPropertyRegistry registry) {
        registry.add("db.url", mysql::getJdbcUrl);
        registry.add("db.username", mysql::getUsername);
        registry.add("db.password", mysql::getPassword);
    }

    @Autowired
    private PlayersResDB playersResDB;

    @Autowired
    private UserSessionManager userSessionManager;

    @Autowired
    private BotService botService;

    private Message createMessage(String text, Long chatId, Long userId) {
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(message.getText()).thenReturn(text);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        return message;
    }

    @Test
    void topCommandWithRealGameFlow() {
        Long chatId1 = 11111L;
        Long userId1 = 1L;
        Long chatId2 = 22222L;
        Long userId2 = 2L;

        // Первый игрок
        botService.processMessage(createMessage("/start", chatId1, userId1));
        botService.processMessage(createMessage("/play", chatId1, userId1));
        botService.processMessage(createMessage("a", chatId1, userId1));
        botService.processMessage(createMessage("b", chatId1, userId1));
        botService.processMessage(createMessage("c", chatId1, userId1));

        int gold1_session = userSessionManager.getUserSession(chatId1).getAmountOfGold();

        // Второй игрок
        botService.processMessage(createMessage("/start", chatId2, userId2));
        botService.processMessage(createMessage("/play", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));

        int gold2_session = userSessionManager.getUserSession(chatId2).getAmountOfGold();

        int gold1_db = playersResDB.getPlayerValue(chatId1, PlayersResDB.GOLD_KEY);
        int gold2_db = playersResDB.getPlayerValue(chatId2, PlayersResDB.GOLD_KEY);

        assertEquals(gold1_session, gold1_db);
        assertEquals(gold2_session, gold2_db);

        // Проверяем /top
        String leaderboard = botService.processMessage(createMessage("/top", chatId1, userId1));

        assertTrue(leaderboard.contains("ТОП ИГРОКОВ ПО ЗОЛОТУ"));

        String key1 = "!!" + chatId1 + "!!";
        String key2 = "!!" + chatId2 + "!!";

        assertTrue(leaderboard.contains(key1));
        assertTrue(leaderboard.contains(key2));

        if (gold1_db > gold2_db) {
            assertTrue(leaderboard.indexOf(key1) < leaderboard.indexOf(key2));
        } else if (gold2_db > gold1_db) {
            assertTrue(leaderboard.indexOf(key2) < leaderboard.indexOf(key1));
        }
    }
}