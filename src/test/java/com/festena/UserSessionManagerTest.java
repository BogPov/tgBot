package com.festena;

import com.festena.Session.UserSession;
import com.festena.databases.IDataBase;
import com.festena.manager.EnergyManager;
import com.festena.manager.UserSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSessionManagerTest {

    private UserSessionManager userSessionManager;

    @Mock
    private IDataBase playersResDB;

    @Mock
    private EnergyManager energyManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(playersResDB.isPlayerExists(anyLong())).thenReturn(false);
        userSessionManager = new UserSessionManager(playersResDB, energyManager);
    }

    @Test
    void constructorShouldInitializeEmptySessionsMap() {
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

        verify(playersResDB).isPlayerExists(chatId);
        verify(playersResDB).addPlayer(chatId);
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

        userSessionManager.addSession(chatIdToRemove, userIdToRemove);
        userSessionManager.addSession(1L, 2L);

        assertTrue(userSessionManager.isSessionExist(chatIdToRemove));
        assertEquals(2, userSessionManager.getAllSessions().size());

        userSessionManager.removeSession(chatIdToRemove);

        assertFalse(userSessionManager.isSessionExist(chatIdToRemove));
        assertEquals(1, userSessionManager.getAllSessions().size());
        assertTrue(userSessionManager.isSessionExist(1L));
    }
}