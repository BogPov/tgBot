package com.festena;

import com.festena.Session.UserSession;
import com.festena.manager.TextManager;
import com.festena.manager.UserSessionManager;
import com.festena.service.BotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BotServiceTest {

    @Mock
    private UserSessionManager userSessionManager;
    @Mock
    private TextManager textManager;
    @Mock
    private UserSession userSession;

    @InjectMocks
    private BotService botService;

    private final Long CHAT_ID = 12345L;
    private final Long USER_ID = 67890L;

    private Message createMockMessage(String text, Long chatId, Long userId) {
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(message.getText()).thenReturn(text);
        when(message.getChatId()).thenReturn(chatId);
        lenient().when(message.getFrom()).thenReturn(user);
        lenient().when(user.getId()).thenReturn(userId);

        return message;
    }

    //сессии нет, команда /start
    @Test
    void startNewUser() {
        Message message = createMockMessage("/start", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(false);
        when(textManager.getText("welcome_message")).thenReturn("Welcome to the bot!");

        String response = botService.processMessage(message);

        assertEquals("Welcome to the bot!", response);
    }

    //сессия есть, команда /start
    @Test
    void startExistingUser() {
        Message message = createMockMessage("/start", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);

        String response = botService.processMessage(message);

        assertEquals("Бро, ты уже запустил бота!", response);
    }

    //существующий пользователь отправляет /play
    @Test
    void playExistingUser() {
        Message message = createMockMessage("/play", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.getNextEventText()).thenReturn("New event started: Choose wisely!");

        String response = botService.processMessage(message);

        assertEquals("New event started: Choose wisely!", response);
    }

    //существующий пользователь отправляет /lore
    @Test
    void loreExistingUser() {
        Message message = createMockMessage("/lore", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(textManager.getText("lore")).thenReturn("This is the rich lore of the game.");

        String response = botService.processMessage(message);

        assertEquals("This is the rich lore of the game.", response);
    }

    //существующий пользователь отправляет /help
    @Test
    void helpExistingUser() {
        Message message = createMockMessage("/help", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(textManager.getText("help")).thenReturn("Available commands: /start, /play, /help...");

        String response = botService.processMessage(message);

        assertEquals("Available commands: /start, /play, /help...", response);
    }

    //существующий пользователь отправляет /res
    @Test
    void resExistingUser() {
        Message message = createMockMessage("/res", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.getResForTab()).thenReturn("Gold: 100, Food: 50, People: 20...");

        String response = botService.processMessage(message);

        assertEquals("Gold: 100, Food: 50, People: 20...", response);
    }

    //существующий пользователь отправляет /top
    @Test
    void topExistingUser() {
        Message message = createMockMessage("/top", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);

        //фиктивные сессии для имитации нескольких игроков
        HashMap<Long, UserSession> sessions = new HashMap<>();
        UserSession session1 = mock(UserSession.class);
        when(session1.getAmountOfGold()).thenReturn(200);
        sessions.put(CHAT_ID, session1); //cессия пользователя
        UserSession session2 = mock(UserSession.class);
        when(session2.getAmountOfGold()).thenReturn(150);
        sessions.put(CHAT_ID + 1, session2); //cессия другого пользователя

        when(userSessionManager.getAllSessions()).thenReturn(sessions);

        String response = botService.processMessage(message);

        String expectedLeaderboard = "ТОП ИГРОКОВ ПО ЗОЛОТУ\n" +
                "!!" + CHAT_ID + "!! - 200\n" +
                "!!" + (CHAT_ID + 1) + "!! - 150\n";
        assertEquals(expectedLeaderboard, response);
    }

    //существующий пользователь отправляет неизвестную команду
    @Test
    void unknownCommand() {
        Message message = createMockMessage("/unknowncommand", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);

        String response = botService.processMessage(message);

        assertEquals("бро, я такой команды не знаю. Напиши /help", response, "Должно быть возвращено сообщение о неизвестной команде.");
    }

    //новый пользователь отправляет неизвестную команду
    @Test
    void unknownCommandNewUser() {
        Message message = createMockMessage("/unknowncommand", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(false);

        String response = botService.processMessage(message);

        assertEquals("", response);
    }

    //тесты обработки некомандных сообщений

    //новый пользователь отправляет текст
    @Test
    void nonCommandNoSession() {
        Message message = createMockMessage("Hello!", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(false);

        String response = botService.processMessage(message);

        assertEquals("Перед тем как отправлять сообщения запусти бота!: /start", response);
    }

    //существующий пользователь отправляет текст, но у него нет активного события
    @Test
    void noCurrentEvent() {
        Message message = createMockMessage("Text", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(false);

        String response = botService.processMessage(message);

        assertEquals("Перед тем как писать сообщения, начни игру: /play", response);
    }

    //существующий пользователь отправляет вариант ответа на активное событие
    @Test
    void validAnswer() {
        Message message = createMockMessage("a", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(userSession.processPlayerAnswer("a")).thenReturn("You chose 'a'");

        String response = botService.processMessage(message);

        assertEquals("You chose 'a'", response);
    }

    //нечувствительность к регистру
    @Test
    void validAnswerCaseInsensitive() {
        Message message = createMockMessage("A", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(userSession.processPlayerAnswer("A")).thenReturn("You chose 'A'");

        String response = botService.processMessage(message);

        assertEquals("You chose 'A'", response);
    }

    //существующий пользователь отправляет не вариант ответа на активное событие
    @Test
    void invalidAnswer() {
        Message message = createMockMessage("invalid", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(userSession.getCurrentEventText()).thenReturn("Current event: What will you do?");

        String response = botService.processMessage(message);

        assertEquals("Ответ на событие должен быть написан одной латинцкой буквой!\n\nCurrent event: What will you do?", response);
    }

    //проверка на число
    @Test
    void invalidAnswerDigit() {
        Message message = createMockMessage("1", CHAT_ID, USER_ID); // Проверка с цифрой
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(userSession.getCurrentEventText()).thenReturn("Current event: Choose option 1, 2, 3, or 4.");

        String response = botService.processMessage(message);

        assertEquals("Ответ на событие должен быть написан одной латинцкой буквой!\n\nCurrent event: Choose option 1, 2, 3, or 4.", response);
    }
}
