package com.festena;

import com.festena.manager.UserSessionManager;
import com.festena.service.BotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BotServiceTopCommandTest {

    @Autowired
    private BotService botService;

    @Autowired
    private UserSessionManager userSessionManager;

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

        // сценарий первого игрока
        botService.processMessage(createMessage("/start", chatId1, userId1));
        botService.processMessage(createMessage("/play", chatId1, userId1));
        botService.processMessage(createMessage("a", chatId1, userId1));
        botService.processMessage(createMessage("b", chatId1, userId1));
        botService.processMessage(createMessage("c", chatId1, userId1));

        int gold1 = userSessionManager.getUserSession(chatId1).getAmountOfGold();

        // сценарий второго игрока
        botService.processMessage(createMessage("/start", chatId2, userId2));
        botService.processMessage(createMessage("/play", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));
        botService.processMessage(createMessage("a", chatId2, userId2));

        int gold2 = userSessionManager.getUserSession(chatId2).getAmountOfGold();

        // смотрим что дает /top
        String leaderboard = botService.processMessage(createMessage("/top", chatId1, userId1));

        // проверки
        assertTrue(leaderboard.contains("ТОП ИГРОКОВ ПО ЗОЛОТУ"));

        if (gold1 > gold2) {
            assertTrue(
                    leaderboard.indexOf(chatId1.toString()) <
                            leaderboard.indexOf(chatId2.toString())
            );
        } else {
            assertTrue(
                    leaderboard.indexOf(chatId2.toString()) <
                            leaderboard.indexOf(chatId1.toString())
            );
        }
    }
}
