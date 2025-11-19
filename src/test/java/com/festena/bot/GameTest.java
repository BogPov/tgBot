package com.festena.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

public class GameTest {
    private Game game;
    private EventManager eventManager;
    private List<Event> allEvents;

    @Before
    public void setUp() throws Exception {
        game = new Game();
        // все события из events.json
        try (InputStream inputStream = getClass().getResourceAsStream("/events.json")) {
            allEvents = new ObjectMapper().readValue(inputStream, new TypeReference<List<Event>>() {});
        }
        eventManager = game.getEventManager();
    }

    //установка текущего события в EventManager по индексу
    private void setCurrentEvent(int eventIndex) {
        Event targetEvent = allEvents.get(eventIndex);
        eventManager.setCurrentEvent(targetEvent);
    }

    //событие 1: "Крестьяне жалуются на неурожай"
    @Test
    public void testEvent1OptionAResource() {
        setCurrentEvent(0);
        Option expectedOption = allEvents.get(0).options.get(0);

        int initialFood = game.getResources().getFood();
        int initialReputation = game.getResources().getReputation();
        int initialPeople = game.getResources().getPeople();

        game.processPlayerAnswer("A");

        assertEquals(initialFood + expectedOption.food, game.getResources().getFood());
        assertEquals(initialReputation + expectedOption.reputation, game.getResources().getReputation());
        assertEquals(initialPeople + expectedOption.people, game.getResources().getPeople());
    }

    //событие 2: "Соседнее королевство предлагает союз"
    @Test
    public void testEvent2OptionAResource() {
        setCurrentEvent(1);
        Option expectedOption = allEvents.get(1).options.get(0);

        int initialReputation = game.getResources().getReputation();
        int initialArmy = game.getResources().getArmy();
        int initialPeople = game.getResources().getPeople();
        int initialGold = game.getResources().getGold();

        game.processPlayerAnswer("A");

        assertEquals(initialReputation + expectedOption.reputation, game.getResources().getReputation());
        assertEquals(initialArmy + expectedOption.army, game.getResources().getArmy());
        assertEquals(initialPeople + expectedOption.people, game.getResources().getPeople());
        assertEquals(initialGold + expectedOption.gold, game.getResources().getGold());
    }

    //тест на несуществующую опцию
    @Test
    public void testInvalidOptionNotChangeResources() {
        setCurrentEvent(0);

        int initialFood = game.getResources().getFood();
        int initialReputation = game.getResources().getReputation();
        int initialPeople = game.getResources().getPeople();
        int initialGold = game.getResources().getGold();
        int initialArmy = game.getResources().getArmy();
        int initialTechnology = game.getResources().getTechnology();

        game.processPlayerAnswer("X");

        assertEquals(initialFood, game.getResources().getFood());
        assertEquals(initialReputation, game.getResources().getReputation());
        assertEquals(initialPeople, game.getResources().getPeople());
        assertEquals(initialGold, game.getResources().getGold());
        assertEquals(initialArmy, game.getResources().getArmy());
        assertEquals(initialTechnology, game.getResources().getTechnology());
    }
}
