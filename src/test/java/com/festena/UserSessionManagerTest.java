package com.festena;

import com.festena.Session.UserSession;
import com.festena.manager.UserSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionManagerTest {

    private UserSessionManager userSessionManager;

    @BeforeEach
    void setUp() {
        userSessionManager = new UserSessionManager();
    }

    @Test
    void constructorShouldInitializeEmptySessionsMap() {
        //после создания менеджера карта сессий пуста
        assertNotNull(userSessionManager.getAllSessions());
        assertTrue(userSessionManager.getAllSessions().isEmpty());
        assertEquals(0, userSessionManager.getAllSessions().size());
    }

    @Test
    void addNewSession() {
        Long chatId = 123L;
        Long userId = 456L;

        userSessionManager.addSession(chatId, userId);

        assertTrue(userSessionManager.isSessionExist(chatId));
        assertEquals(1, userSessionManager.getAllSessions().size());

        UserSession session = userSessionManager.getUserSession(chatId);
        assertNotNull(session);
        assertEquals(chatId, session.getChatId());
        assertEquals(userId, session.getUserId());
    }

    @Test
    void isSessionExistShouldReturnTrueForExistingSession() {
        Long chatId = 100L;
        Long userId = 200L;
        userSessionManager.addSession(chatId, userId);

        assertTrue(userSessionManager.isSessionExist(chatId));
    }

    @Test
    void isSessionExistShouldReturnFalseForNonExistingSession() {
        assertFalse(userSessionManager.isSessionExist(999L));
    }

    @Test
    void removeSession() {
        Long chatIdToRemove = 789L;
        Long userIdToRemove = 1011L;
        userSessionManager.addSession(chatIdToRemove, userIdToRemove);//для удаления
        userSessionManager.addSession(1L, 2L); //чтобы убедиться, что удаляется нужная сессия

        //существует до удаления
        assertTrue(userSessionManager.isSessionExist(chatIdToRemove));
        assertEquals(2, userSessionManager.getAllSessions().size());

        userSessionManager.removeSession(chatIdToRemove);

        assertFalse(userSessionManager.isSessionExist(chatIdToRemove));
        assertEquals(1, userSessionManager.getAllSessions().size());
        assertTrue(userSessionManager.isSessionExist(1L));
    }
}