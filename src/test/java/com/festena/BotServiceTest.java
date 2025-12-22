package com.festena;

import com.festena.Session.UserSession;
import com.festena.databases.IPlayerEnergyDataBase;
import com.festena.fakeDBs.FakePlayerEnergyDB;
import com.festena.manager.EnergyManager;
import com.festena.manager.TextManager;
import com.festena.manager.UserSessionManager;
import com.festena.service.BotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

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

    private IPlayerEnergyDataBase energyDb;
    private EnergyManager energyManager;
    private BotService botService;

    private final Long CHAT_ID = 12345L;
    private final Long USER_ID = 67890L;

    @BeforeEach
    void setUp() {
        energyDb = new FakePlayerEnergyDB();
        energyDb.addPlayerToDB(CHAT_ID);
        energyManager = spy(new EnergyManager(energyDb));
        botService = new BotService(userSessionManager, textManager, energyManager);
    }

    private Message createMockMessage(String text, Long chatId, Long userId) {
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(message.getText()).thenReturn(text);
        when(message.getChatId()).thenReturn(chatId);
        lenient().when(message.getFrom()).thenReturn(user);
        lenient().when(user.getId()).thenReturn(userId);

        return message;
    }

    private static String stripLuckSuffix(String response) {
        int idx = response.indexOf(BotService.STRING_DIVIDOR);
        if (idx >= 0) return response.substring(0, idx);
        return response;
    }

    private static String stripNeedAnswerBtns(String response) {
        return response.replace(BotService.NEED_ANSWER_BTNS_SYMBOL, "");
    }

    private static boolean isLuckMessage(String s) {
        return s.contains("Вам повезло и вы выбили день на отдых +10 энергии")
                || s.contains("Вам повезло и вы выбили полдня на отдых +5 энергии")
                || s.contains("Вам повезло и вы выбили часок на отдых +1 энергия");
    }

    @Test
    void startNewUser() {
        Message message = createMockMessage("/start", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(false);
        when(textManager.getText("welcome_message")).thenReturn("Welcome to the bot!");

        String response = botService.processMessage(message);

        assertEquals("Welcome to the bot!", response);
    }

    @Test
    void startExistingUser() {
        Message message = createMockMessage("/start", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);

        String response = botService.processMessage(message);

        assertEquals("Бро, ты уже запустил бота!", response);
    }

    @Test
    void playExistingUser() {
        Message message = createMockMessage("/play", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.getNextEventText()).thenReturn("New event started: Choose wisely!");

        String response = botService.processMessage(message);

        assertEquals("New event started: Choose wisely!", stripNeedAnswerBtns(response));
    }

    @Test
    void loreExistingUser() {
        Message message = createMockMessage("/lore", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(textManager.getText("lore")).thenReturn("This is the rich lore of the game.");

        String response = botService.processMessage(message);

        assertEquals("This is the rich lore of the game.", response);
    }

    @Test
    void helpExistingUser() {
        Message message = createMockMessage("/help", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(textManager.getText("help")).thenReturn("Available commands: /start, /play, /help...");

        String response = botService.processMessage(message);

        assertEquals("Available commands: /start, /play, /help...", response);
    }

    @Test
    void resExistingUser() {
        Message message = createMockMessage("/res", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.getResForTab()).thenReturn("Gold: 100, Food: 50, People: 20...");

        String response = botService.processMessage(message);

        assertEquals("Gold: 100, Food: 50, People: 20...", response);
    }

    @Test
    void unknownCommand() {
        Message message = createMockMessage("/unknowncommand", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);

        String response = botService.processMessage(message);

        assertEquals("бро, я такой команды не знаю. Напиши /help", response);
    }

    @Test
    void unknownCommandNewUser() {
        Message message = createMockMessage("/unknowncommand", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(false);

        String response = botService.processMessage(message);

        assertEquals("", response);
    }

    @Test
    void nonCommandNoSession() {
        Message message = createMockMessage("Hello!", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(false);

        String response = botService.processMessage(message);

        assertEquals("Перед тем как отправлять сообщения запусти бота!: /start", response);
    }

    @Test
    void noCurrentEvent() {
        Message message = createMockMessage("Text", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(false);

        String response = botService.processMessage(message);

        assertEquals("Перед тем как писать сообщения, начни игру: /play", response);
    }

    @Test
    void validAnswer() {
        Message message = createMockMessage("a", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(energyManager.couldPlayerMakeATerm(CHAT_ID)).thenReturn(true);
        when(userSession.processPlayerAnswer("a")).thenReturn("You chose 'a'");

        String response = botService.processMessage(message);

        response = stripLuckSuffix(response);

        assertEquals("You chose 'a'", stripNeedAnswerBtns(response));
        verify(userSessionManager).updatePlayerInDB(CHAT_ID);
        verify(energyManager).addEnergyToPlayer(CHAT_ID, -1);
    }

    @Test
    void validAnswerCaseInsensitive() {
        Message message = createMockMessage("A", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(energyManager.couldPlayerMakeATerm(CHAT_ID)).thenReturn(true);
        when(userSession.processPlayerAnswer("A")).thenReturn("You chose 'A'");

        String response = botService.processMessage(message);

        String withoutLuck = stripLuckSuffix(response);

        assertEquals("You chose 'A'", stripNeedAnswerBtns(withoutLuck));
        if (!withoutLuck.equals(response)) {
            String luckPart = response.substring(withoutLuck.length());
            assertEquals(true, isLuckMessage(luckPart));
        }
    }

    @Test
    void invalidAnswer() {
        Message message = createMockMessage("invalid", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(userSession.getCurrentEventText()).thenReturn("Current event: What will you do?");

        String response = botService.processMessage(message);

        assertEquals("Ответ на событие должен быть написан одной латинцкой буквой!\n\n@@@Current event: What will you do?", response);
    }

    @Test
    void invalidAnswerDigit() {
        Message message = createMockMessage("1", CHAT_ID, USER_ID);
        when(userSessionManager.isSessionExist(CHAT_ID)).thenReturn(true);
        when(userSessionManager.getUserSession(CHAT_ID)).thenReturn(userSession);
        when(userSession.hasCurrentEvent()).thenReturn(true);
        when(userSession.getCurrentEventText()).thenReturn("Current event: Choose option 1, 2, 3, or 4.");

        String response = botService.processMessage(message);

        assertEquals("Ответ на событие должен быть написан одной латинцкой буквой!\n\n@@@Current event: Choose option 1, 2, 3, or 4.", response);
    }
}