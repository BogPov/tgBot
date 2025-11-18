package com.festena.bot;

import java.util.HashMap;

public class Game {
    private Resources resources;
    private EventManager eventManager = new EventManager();
    
    private static final String PEOPLE_ICON = "👥Население ";
    private static final String GOLD_ICON = " | 💰Золото ";
    private static final String FOOD_ICON = " | 🌾Продовольствие ";
    private static final String ARMY_ICON = " | ⚔ Армия ";
    private static final String TECHNOLOGY_ICON = " | ⚙ Технологии ";
    private static final String REPUTATION_ICON = " | 🏅Репутация ";

    public Game() {
        this.resources = new Resources();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public String getResForTab() {
        return PEOPLE_ICON + resources.people
                + GOLD_ICON + resources.gold
                + FOOD_ICON + resources.food
                + ARMY_ICON + resources.army
                + TECHNOLOGY_ICON + resources.technology
                + REPUTATION_ICON + resources.reputation;
    }

    public void processPlayerAnswer(String playerResponse) {
        HashMap<String, Integer> resourceChanges = eventManager.getResourceChanges(playerResponse);
        resources.gold += resourceChanges.get("gold");
        resources.people += resourceChanges.get("people");
        resources.food += resourceChanges.get("food");
        resources.reputation += resourceChanges.get("reputation");
        resources.technology += resourceChanges.get("technology");
        resources.army += resourceChanges.get("army");
    }

    public String getNextEventText() {
        eventManager.startNewEvent();
        return eventManager.getCurrentEventText();
    }
}

class Resources {
    int people;
    int food;
    int army;
    int gold;
    int reputation;
    int technology;

    public Resources() {
        this.people = 100;
        this.army = 100;
        this.food = 100;
        this.gold = 1000;
        this.reputation = 50;
        this.technology = 0;
    }
}
