package com.festena.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.List;

class EventManagerTest {

    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
    }

    @Test
    void testConstructorLoadsEvents() {
        List<Event> storage = eventManager.getEventStorage();

        assertNotNull(storage);
        assertFalse(storage.isEmpty());
    }

    @Test
    void testSetCurrentEvent() {
        Event event = new Event("Test Legend");
        eventManager.setCurrentEvent(event);

        Event currentEvent = eventManager.getCurrentEvent();

        assertSame(event, currentEvent);
    }

    @Test
    void testGetCurrentEventTextWhenNull() {
        assertEquals("", eventManager.getCurrentEventText());
    }

    @Test
    void testGetCurrentEventTextWithEvent() {
        eventManager.startNewEvent();
        String text = eventManager.getCurrentEventText();

        assertNotNull(text);
        assertFalse(text.isEmpty());

        // Проверяем что метод не падает с null options
        Event eventWithNullOptions = new Event("Legend");
        eventWithNullOptions.setOptions(null);


        eventManager.setCurrentEvent(eventWithNullOptions);
        text = eventManager.getCurrentEventText();
        assertNotNull(text);
        assertTrue(text.contains("Legend"));
    }

    @Test
    void testStartNewEvent() {
        eventManager.startNewEvent();

        Event currentEvent = eventManager.getCurrentEvent();

        assertNotNull(currentEvent);
    }

    @Test
    void testStartNewEventThrowsWhenEmpty() {
        List<Event> storage = eventManager.getEventStorage();
        storage.clear();

        assertThrows(IllegalStateException.class, () -> eventManager.startNewEvent());
    }

    @Test
    void testGetResourceChangesForExistingOption() {
        eventManager.startNewEvent();

        // Получаем текущее событие чтобы узнать какие опции есть
        Event currentEvent = eventManager.getCurrentEvent();
        List<Option> options = currentEvent.getOptions();

        if (options != null && !options.isEmpty()) {
            // Берем первую опцию
            String firstOptionId = options.get(0).getId();

            Map<String, Integer> changes = eventManager.getResourceChanges(firstOptionId);

            assertNotNull(changes);
            assertEquals(6, changes.size()); // Всегда 6 ресурсов

            // Проверяем что все ключи есть
            assertTrue(changes.containsKey("gold"));
            assertTrue(changes.containsKey("people"));
            assertTrue(changes.containsKey("food"));
            assertTrue(changes.containsKey("army"));
            assertTrue(changes.containsKey("technology"));
            assertTrue(changes.containsKey("reputation"));
        }
    }

    @Test
    void testGetResourceChangesForNonExistingOption() {
        eventManager.startNewEvent();

        Map<String, Integer> changes = eventManager.getResourceChanges("NON_EXISTENT_OPTION_999");

        assertNotNull(changes);
        assertEquals(0, changes.get("gold"));
        assertEquals(0, changes.get("people"));
        assertEquals(0, changes.get("food"));
        assertEquals(0, changes.get("army"));
        assertEquals(0, changes.get("technology"));
        assertEquals(0, changes.get("reputation"));
    }

    @Test
    void testGetResourceChangesWhenNoCurrentEvent() {
        Map<String, Integer> changes = eventManager.getResourceChanges("A");

        assertNotNull(changes);
        assertEquals(0, changes.get("gold"));
        assertEquals(0, changes.get("people"));
        assertEquals(0, changes.get("food"));
        assertEquals(0, changes.get("army"));
        assertEquals(0, changes.get("technology"));
        assertEquals(0, changes.get("reputation"));
    }

    @Test
    void testGetResourceChangesWithSpacesInId() {
        eventManager.startNewEvent();

        Event currentEvent = eventManager.getCurrentEvent();
        List<Option> options = currentEvent.getOptions();

        if (options != null && !options.isEmpty()) {
            String firstOptionId = options.get(0).getId();

            // Проверяем с пробелами
            Map<String, Integer> changes1 = eventManager.getResourceChanges(" " + firstOptionId + " ");
            Map<String, Integer> changes2 = eventManager.getResourceChanges(firstOptionId);

            // Результаты должны быть одинаковыми
            assertEquals(changes1.get("gold"), changes2.get("gold"));
        }
    }

    @Test
    void testGetResourceChangesWithNullOptions() {
        Event event = new Event("Test");
        event.setOptions(null);

        eventManager.setCurrentEvent(event);

        Map<String, Integer> changes = eventManager.getResourceChanges("A");

        assertNotNull(changes);
        assertEquals(0, changes.get("gold"));
    }

    @Test
    void testEventGetters() {
        Event event = new Event();
        event.setLegend("Legend");
        event.setOptions(null);

        assertEquals("Legend", event.getLegend());
        assertNull(event.getOptions());

        // С options
        event.setOptions(List.of());
        assertNotNull(event.getOptions());
    }

    @Test
    void testOptionGetters() {
        Option option = new Option();
        option.setId("ID");
        option.setText("Text");
        option.setGold(100);
        option.setPeople(50);
        option.setFood(30);
        option.setArmy(20);
        option.setTechnology(10);
        option.setReputation(5);

        assertEquals("ID", option.getId());
        assertEquals("Text", option.getText());
        assertEquals(100, option.getGold());
        assertEquals(50, option.getPeople());
        assertEquals(30, option.getFood());
        assertEquals(20, option.getArmy());
        assertEquals(10, option.getTechnology());
        assertEquals(5, option.getReputation());
    }

    @Test
    void testOptionUnpackEffects() {
        Option option = new Option();

        // Полный Map
        Map<String, Integer> fullEffects = Map.of(
                "gold", 100,
                "people", 50,
                "food", 30,
                "army", 20,
                "technology", 10,
                "reputation", 5
        );

        option.unpackEffects(fullEffects);

        assertEquals(100, option.getGold());
        assertEquals(50, option.getPeople());
        assertEquals(30, option.getFood());
        assertEquals(20, option.getArmy());
        assertEquals(10, option.getTechnology());
        assertEquals(5, option.getReputation());
    }

    @Test
    void testOptionUnpackEffectsWithNull() {
        Option option = new Option();

        // null Map
        option.unpackEffects(null);

        assertEquals(0, option.getGold());
        assertEquals(0, option.getPeople());
        assertEquals(0, option.getFood());
        assertEquals(0, option.getArmy());
        assertEquals(0, option.getTechnology());
        assertEquals(0, option.getReputation());
    }

    @Test
    void testOptionUnpackEffectsPartial() {
        Option option = new Option();

        // Неполный Map
        Map<String, Integer> partialEffects = Map.of(
                "gold", 100,
                "food", 50
        );

        option.unpackEffects(partialEffects);

        assertEquals(100, option.getGold());
        assertEquals(0, option.getPeople()); // default
        assertEquals(50, option.getFood());
        assertEquals(0, option.getArmy()); // default
        assertEquals(0, option.getTechnology()); // default
        assertEquals(0, option.getReputation()); // default
    }
}