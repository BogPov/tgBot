package com.festena;

import com.festena.manager.EventManager;
import com.festena.Session.UserSession;
import com.festena.Session.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSessionTest {

    private UserSession userSession;
    private final Long testChatId = 123L;
    private final Long testUserId = 456L;

    @Mock
    private EventManager mockEventManager;

    @BeforeEach
    void setUp() {
        userSession = new UserSession(testChatId, testUserId, mockEventManager);
    }

    @Test
    void userSessionCreation() {
        assertNotNull(userSession);
        assertEquals(testChatId, userSession.getChatId());
        assertEquals(testUserId, userSession.getUserId());

        Resources initialResources = userSession.getResources();

        assertEquals(100, initialResources.getPeople());
        assertEquals(1000, initialResources.getGold());
        assertEquals(100, initialResources.getFood());
        assertEquals(100, initialResources.getArmy());
        assertEquals(0, initialResources.getTechnology());
        assertEquals(50, initialResources.getReputation());
    }

    @Test
    void getResForTabInitialValues() {
        String expected = "👥Население 100 | 💰Золото 1000 | 🌾Продовольствие 100 | ⚔ Армия 100 | ⚙ Технологии 0 | 🏅Репутация 50";

        assertEquals(expected, userSession.getResForTab());
    }

    @Test
    void getChangedResForTabPositiveChanges() {
        Map<String, Integer> changes = new HashMap<>();
        changes.put("people", 10);
        changes.put("gold", 50);
        changes.put("food", 0);
        changes.put("army", 20);
        changes.put("technology", 5);
        changes.put("reputation", 1);

        String expected = "👥Население +10 | 💰Золото +50 | ⚔ Армия +20 | ⚙ Технологии +5 | 🏅Репутация +1";

        assertEquals(expected, userSession.getChangedResForTab(changes));
    }

    @Test
    void getChangedResForTabNegativeChanges() {
        Map<String, Integer> changes = new HashMap<>();
        changes.put("food", -20);
        changes.put("army", -5);
        changes.put("reputation", -1);

        String expected = " | 🌾Продовольствие -20 | ⚔ Армия -5 | 🏅Репутация -1";

        assertEquals(expected, userSession.getChangedResForTab(changes));
    }

    @Test
    void getChangedResForTabMixedChanges() {
        Map<String, Integer> changes = new HashMap<>();
        changes.put("people", 5);
        changes.put("gold", -10);
        changes.put("food", 0);
        changes.put("army", 7);
        changes.put("technology", -2);
        changes.put("reputation", 0);

        String expected = "👥Население +5 | 💰Золото -10 | ⚔ Армия +7 | ⚙ Технологии -2";

        assertEquals(expected, userSession.getChangedResForTab(changes));
    }

    @Test
    void getChangedResForTabNoChangesOrNullValues() {
        Map<String, Integer> changes = new HashMap<>();
        changes.put("gold", 0);
        changes.put("people", 0);
        changes.put("food", null);

        String expected = "";

        assertEquals(expected, userSession.getChangedResForTab(changes));
    }

    @Test
    void processPlayerAnswerValidOption() {
        Map<String, Integer> resourceChangesMap = new HashMap<>();
        resourceChangesMap.put("gold", 100);
        resourceChangesMap.put("people", 10);
        resourceChangesMap.put("food", 0);
        resourceChangesMap.put("army", -5);
        resourceChangesMap.put("technology", 2);
        resourceChangesMap.put("reputation", 5);

        when(mockEventManager.getResourceChanges("A")).thenReturn(resourceChangesMap);
        when(mockEventManager.getCurrentEventText()).thenReturn("Текст следующего события.");

        Resources initialSessionResources = userSession.getResources();
        int initialGold = initialSessionResources.getGold();
        int initialPeople = initialSessionResources.getPeople();
        int initialFood = initialSessionResources.getFood();
        int initialArmy = initialSessionResources.getArmy();
        int initialTechnology = initialSessionResources.getTechnology();
        int initialReputation = initialSessionResources.getReputation();

        String expectedChangesString = "👥Население +10 | 💰Золото +100 | ⚔ Армия -5 | ⚙ Технологии +2 | 🏅Репутация +5";
        String expectedOutput = expectedChangesString + "\n\nТекст следующего события.";

        String actualOutput = userSession.processPlayerAnswer("A");

        assertEquals(expectedOutput, actualOutput);

        assertEquals(initialGold + resourceChangesMap.get("gold"), userSession.getResources().getGold());
        assertEquals(initialPeople + resourceChangesMap.get("people"), userSession.getResources().getPeople());
        assertEquals(initialFood + resourceChangesMap.get("food"), userSession.getResources().getFood()); // 0 изменений
        assertEquals(initialArmy + resourceChangesMap.get("army"), userSession.getResources().getArmy());
        assertEquals(initialTechnology + resourceChangesMap.get("technology"), userSession.getResources().getTechnology());
        assertEquals(initialReputation + resourceChangesMap.get("reputation"), userSession.getResources().getReputation());
    }

    @Test
    void processPlayerAnswer_InvalidOption() {
        Map<String, Integer> noChangesMap = new HashMap<>();
        noChangesMap.put("gold", 0);
        noChangesMap.put("people", 0);
        noChangesMap.put("food", 0);
        noChangesMap.put("army", 0);
        noChangesMap.put("technology", 0);
        noChangesMap.put("reputation", 0);

        when(mockEventManager.getResourceChanges("X")).thenReturn(noChangesMap);
        when(mockEventManager.getCurrentEventText()).thenReturn("Следующее событие после неверного ответа.");

        int initialGold = userSession.getResources().getGold();
        int initialPeople = userSession.getResources().getPeople();
        int initialFood = userSession.getResources().getFood();
        int initialArmy = userSession.getResources().getArmy();
        int initialTechnology = userSession.getResources().getTechnology();
        int initialReputation = userSession.getResources().getReputation();

        String expectedChangesString = "";
        String expectedOutput = expectedChangesString + "\n\nСледующее событие после неверного ответа.";

        String actualOutput = userSession.processPlayerAnswer("X");

        assertEquals(expectedOutput, actualOutput);

        assertEquals(initialGold, userSession.getResources().getGold());
        assertEquals(initialPeople, userSession.getResources().getPeople());
        assertEquals(initialFood, userSession.getResources().getFood());
        assertEquals(initialArmy, userSession.getResources().getArmy());
        assertEquals(initialTechnology, userSession.getResources().getTechnology());
        assertEquals(initialReputation, userSession.getResources().getReputation());
    }

    @Test
    void hasCurrentEventTrue() {
        when(mockEventManager.getCurrentEventText()).thenReturn("Есть текущее событие.");

        assertTrue(userSession.hasCurrentEvent());
    }

    @Test
    void hasCurrentEventFalse() {
        when(mockEventManager.getCurrentEventText()).thenReturn("");

        assertFalse(userSession.hasCurrentEvent());
    }

    @Test
    void getAmountGold() {
        userSession.getResources().addGold(500);

        assertEquals(1500, userSession.getAmountOfGold().intValue());
    }

    @Test
    void getCurrentEventText() {
        String eventText = "Текст текущего события.";
        when(mockEventManager.getCurrentEventText()).thenReturn(eventText);

        assertEquals(eventText, userSession.getCurrentEventText());
    }
}