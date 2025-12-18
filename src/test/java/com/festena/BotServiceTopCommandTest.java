package com.festena;

import com.festena.manager.DataBaseManager;
import com.festena.manager.TextManager;
import com.festena.manager.UserSessionManager;
import com.festena.service.BotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(SystemStubsExtension.class)
class BotServiceTopCommandTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @SystemStub
    private EnvironmentVariables env;

    private BotService botService;
    private UserSessionManager userSessionManager;
    private TextManager textManager;

    @BeforeEach
    void setup() {
        String dbUrl = "mysql://" + mysql.getHost() + ":" + mysql.getMappedPort(3306) + "/" + mysql.getDatabaseName();
        env.set("DB_URL", dbUrl);
        env.set("DB_USERNAME", mysql.getUsername());
        env.set("DB_PASSWORD", mysql.getPassword());

        userSessionManager = new UserSessionManager();
        textManager = new TextManager();
        botService = new BotService(userSessionManager, textManager);
    }

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

        botService.processMessage(createMessage("/start", chatId1, userId1));
        botService.processMessage(createMessage("/play", chatId1, userId1));
        botService.processMessage(createMessage("a", chatId1, userId1));
        botService.processMessage(createMessage("b", chatId1, userId1));
        botService.processMessage(createMessage("c", chatId1, userId1));

        int gold1_session = userSessionManager.getUserSession(chatId1).getAmountOfGold();

        botService.processMessage(createMessage("/start", chatId2, userId2));
        botService.processMessage(createMessage("/play", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));

        int gold2_session = userSessionManager.getUserSession(chatId2).getAmountOfGold();

        DataBaseManager db = new DataBaseManager();
        int gold1_db = db.getPlayerValue(chatId1, DataBaseManager.GOLD_KEY);
        int gold2_db = db.getPlayerValue(chatId2, DataBaseManager.GOLD_KEY);

        assertEquals(gold1_session, gold1_db);
        assertEquals(gold2_session, gold2_db);

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
        } else {
            assertTrue(true);
        }
    }
}
