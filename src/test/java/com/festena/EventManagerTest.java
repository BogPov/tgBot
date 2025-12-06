package com.festena.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;

class EventManagerTest {

    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
    }

    @Test
    void testConstructorLoadsEvents() throws Exception {
        Field storageField = EventManager.class.getDeclaredField("eventStorage");
        storageField.setAccessible(true);
        List<?> storage = (List<?>) storageField.get(eventManager);

        assertNotNull(storage);
        assertFalse(storage.isEmpty());
    }

    @Test
    void testSetCurrentEvent() throws Exception {
        Event event = new Event();
        Field legendField = Event.class.getDeclaredField("legend");
        legendField.setAccessible(true);
        legendField.set(event, "Test Legend");

        eventManager.setCurrentEvent(event);

        Field currentEventField = EventManager.class.getDeclaredField("currentEvent");
        currentEventField.setAccessible(true);
        Event currentEvent = (Event) currentEventField.get(eventManager);

        assertSame(event, currentEvent);
    }

    @Test
    void testGetCurrentEventTextWhenNull() {
        assertEquals("", eventManager.getCurrentEventText());
    }

    @Test
    void testGetCurrentEventTextWithEvent() throws Exception {
        eventManager.startNewEvent();
        String text = eventManager.getCurrentEventText();

        assertNotNull(text);
        assertFalse(text.isEmpty());

        // Проверяем что метод не падает с null options
        Event eventWithNullOptions = new Event();
        Field legendField = Event.class.getDeclaredField("legend");
        legendField.setAccessible(true);
        legendField.set(eventWithNullOptions, "Legend");

        Field optionsField = Event.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        optionsField.set(eventWithNullOptions, null);

        eventManager.setCurrentEvent(eventWithNullOptions);
        text = eventManager.getCurrentEventText();
        assertNotNull(text);
        assertTrue(text.contains("Legend"));
    }

    @Test
    void testStartNewEvent() throws Exception {
        eventManager.startNewEvent();

        Field currentEventField = EventManager.class.getDeclaredField("currentEvent");
        currentEventField.setAccessible(true);
        Event currentEvent = (Event) currentEventField.get(eventManager);

        assertNotNull(currentEvent);
    }

    @Test
    void testStartNewEventThrowsWhenEmpty() throws Exception {
        Field storageField = EventManager.class.getDeclaredField("eventStorage");
        storageField.setAccessible(true);
        List<?> storage = (List<?>) storageField.get(eventManager);
        storage.clear();

        assertThrows(IllegalStateException.class, () -> eventManager.startNewEvent());
    }

    @Test
    void testGetResourceChangesForExistingOption() throws Exception {
        eventManager.startNewEvent();

        // Получаем текущее событие чтобы узнать какие опции есть
        Field currentEventField = EventManager.class.getDeclaredField("currentEvent");
        currentEventField.setAccessible(true);
        Event currentEvent = (Event) currentEventField.get(eventManager);

        Field optionsField = Event.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        List<Option> options = (List<Option>) optionsField.get(currentEvent);

        if (options != null && !options.isEmpty()) {
            // Берем первую опцию
            Field idField = Option.class.getDeclaredField("id");
            idField.setAccessible(true);
            String firstOptionId = (String) idField.get(options.get(0));

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
    void testGetResourceChangesWithSpacesInId() throws Exception {
        eventManager.startNewEvent();

        Field currentEventField = EventManager.class.getDeclaredField("currentEvent");
        currentEventField.setAccessible(true);
        Event currentEvent = (Event) currentEventField.get(eventManager);

        Field optionsField = Event.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        List<Option> options = (List<Option>) optionsField.get(currentEvent);

        if (options != null && !options.isEmpty()) {
            Field idField = Option.class.getDeclaredField("id");
            idField.setAccessible(true);
            String firstOptionId = (String) idField.get(options.get(0));

            // Проверяем с пробелами
            Map<String, Integer> changes1 = eventManager.getResourceChanges(" " + firstOptionId + " ");
            Map<String, Integer> changes2 = eventManager.getResourceChanges(firstOptionId);

            // Результаты должны быть одинаковыми
            assertEquals(changes1.get("gold"), changes2.get("gold"));
        }
    }

    @Test
    void testGetResourceChangesWithNullOptions() throws Exception {
        Event event = new Event();
        Field legendField = Event.class.getDeclaredField("legend");
        legendField.setAccessible(true);
        legendField.set(event, "Test");

        Field optionsField = Event.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        optionsField.set(event, null);

        eventManager.setCurrentEvent(event);

        Map<String, Integer> changes = eventManager.getResourceChanges("A");

        assertNotNull(changes);
        assertEquals(0, changes.get("gold"));
    }

    @Test
    void testEventGetters() throws Exception {
        Event event = new Event();

        Field legendField = Event.class.getDeclaredField("legend");
        legendField.setAccessible(true);
        legendField.set(event, "Legend");

        Field optionsField = Event.class.getDeclaredField("options");
        optionsField.setAccessible(true);
        optionsField.set(event, null);

        assertEquals("Legend", event.getLegend());
        assertNull(event.getOptions());

        // С options
        optionsField.set(event, List.of());
        assertNotNull(event.getOptions());
    }

    @Test
    void testOptionGetters() throws Exception {
        Option option = new Option();

        Field idField = Option.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(option, "ID");

        Field textField = Option.class.getDeclaredField("text");
        textField.setAccessible(true);
        textField.set(option, "Text");

        Field goldField = Option.class.getDeclaredField("gold");
        goldField.setAccessible(true);
        goldField.set(option, 100);

        Field peopleField = Option.class.getDeclaredField("people");
        peopleField.setAccessible(true);
        peopleField.set(option, 50);

        Field foodField = Option.class.getDeclaredField("food");
        foodField.setAccessible(true);
        foodField.set(option, 30);

        Field armyField = Option.class.getDeclaredField("army");
        armyField.setAccessible(true);
        armyField.set(option, 20);

        Field technologyField = Option.class.getDeclaredField("technology");
        technologyField.setAccessible(true);
        technologyField.set(option, 10);

        Field reputationField = Option.class.getDeclaredField("reputation");
        reputationField.setAccessible(true);
        reputationField.set(option, 5);

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
    void testOptionUnpackEffects() throws Exception {
        Option option = new Option();

        Method unpackEffectsMethod = Option.class.getDeclaredMethod("unpackEffects", Map.class);
        unpackEffectsMethod.setAccessible(true);

        // Полный Map
        Map<String, Integer> fullEffects = Map.of(
                "gold", 100,
                "people", 50,
                "food", 30,
                "army", 20,
                "technology", 10,
                "reputation", 5
        );

        unpackEffectsMethod.invoke(option, fullEffects);

        assertEquals(100, option.getGold());
        assertEquals(50, option.getPeople());
        assertEquals(30, option.getFood());
        assertEquals(20, option.getArmy());
        assertEquals(10, option.getTechnology());
        assertEquals(5, option.getReputation());
    }

    @Test
    void testOptionUnpackEffectsWithNull() throws Exception {
        Option option = new Option();

        Method unpackEffectsMethod = Option.class.getDeclaredMethod("unpackEffects", Map.class);
        unpackEffectsMethod.setAccessible(true);

        // null Map
        unpackEffectsMethod.invoke(option, new Object[]{null});

        assertEquals(0, option.getGold());
        assertEquals(0, option.getPeople());
        assertEquals(0, option.getFood());
        assertEquals(0, option.getArmy());
        assertEquals(0, option.getTechnology());
        assertEquals(0, option.getReputation());
    }

    @Test
    void testOptionUnpackEffectsPartial() throws Exception {
        Option option = new Option();

        Method unpackEffectsMethod = Option.class.getDeclaredMethod("unpackEffects", Map.class);
        unpackEffectsMethod.setAccessible(true);

        // Неполный Map
        Map<String, Integer> partialEffects = Map.of(
                "gold", 100,
                "food", 50
        );

        unpackEffectsMethod.invoke(option, partialEffects);

        assertEquals(100, option.getGold());
        assertEquals(0, option.getPeople()); // default
        assertEquals(50, option.getFood());
        assertEquals(0, option.getArmy()); // default
        assertEquals(0, option.getTechnology()); // default
        assertEquals(0, option.getReputation()); // default
    }
}