package com.festena.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

public class GameTest {

    private Game game;
    private EventManager eventManager;
    private List<Event> allEvents;

    @Before
    public void setUp() throws Exception {
        game = new Game();

        // EventManager из Game
        Field eventManagerField = Game.class.getDeclaredField("eventManager");
        eventManagerField.setAccessible(true);
        eventManager = (EventManager) eventManagerField.get(game);

        // все события из events.json
        try (InputStream inputStream = getClass().getResourceAsStream("/events.json")) {
            allEvents = new ObjectMapper().readValue(inputStream, new TypeReference<List<Event>>() {});
        }
    }

    //установка текущего события в EventManager по индексу
    private void setCurrentEvent(int eventIndex) throws Exception {
        Event targetEvent = allEvents.get(eventIndex);
        Field currentEventField = EventManager.class.getDeclaredField("currentEvent");
        currentEventField.setAccessible(true);
        currentEventField.set(eventManager, targetEvent);
    }

    //событие 1: "Крестьяне жалуются на неурожай"

    @Test
    public void testEvent1OptionAResource() throws Exception {
        setCurrentEvent(0);
        Option expectedOption = allEvents.get(0).options.get(0);

        int initialFood = game.resources.food;
        int initialReputation = game.resources.reputation;
        int initialPeople = game.resources.people;

        game.proccesPlAnswer("A");

        assertEquals(initialFood + expectedOption.food, game.resources.food);
        assertEquals(initialReputation + expectedOption.reputation, game.resources.reputation);
        assertEquals(initialPeople + expectedOption.people, game.resources.people);
    }

    //событие 2: "Соседнее королевство предлагает союз"

    @Test
    public void testEvent2OptionAResource() throws Exception {
        setCurrentEvent(1);
        Option expectedOption = allEvents.get(1).options.get(0);

        int initialReputation = game.resources.reputation;
        int initialArmy = game.resources.army;
        int initialPeople = game.resources.people;
        int initialGold = game.resources.gold;

        game.proccesPlAnswer("A");

        assertEquals(initialReputation + expectedOption.reputation, game.resources.reputation);
        assertEquals(initialArmy + expectedOption.army, game.resources.army);
        assertEquals(initialPeople + expectedOption.people, game.resources.people);
        assertEquals(initialGold + expectedOption.gold, game.resources.gold);
    }

    //тест на несуществующую опцию

    @Test
    public void testInvalidOptionNotChangeResources() throws Exception {
        setCurrentEvent(0);

        int initialFood = game.resources.food;
        int initialReputation = game.resources.reputation;
        int initialPeople = game.resources.people;
        int initialGold = game.resources.gold;
        int initialArmy = game.resources.army;
        int initialTechnology = game.resources.technology;

        game.proccesPlAnswer("X");

        assertEquals(initialFood, game.resources.food);
        assertEquals(initialReputation, game.resources.reputation);
        assertEquals(initialPeople, game.resources.people);
        assertEquals(initialGold, game.resources.gold);
        assertEquals(initialArmy, game.resources.army);
        assertEquals(initialTechnology, game.resources.technology);
    }
}
