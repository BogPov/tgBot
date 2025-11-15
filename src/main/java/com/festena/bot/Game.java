package com.festena.bot;

import java.util.HashMap;

public class Game {
    Resources resources;
    EventManager eventManager = new EventManager();

    public Game() {
        this.resources = new Resources();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public String getResForTab() {
        return "👥Население " + this.resources.people
                + " | 💰Золото " + this.resources.gold
                + " | 🌾Продовольствие " + this.resources.food
                + " | ⚔ Армия " + this.resources.army
                + " | ⚙ Технологии " + this.resources.technology
                + " | 🏅Репутация " + this.resources.reputation;
    }

    public void processPlayerAnswer(String player_response) {
        HashMap<String, Integer> resChange = this.eventManager.getResourceChanges(player_response);
        this.resources.gold += resChange.get("gold");
        this.resources.people += resChange.get("people");
        this.resources.food += resChange.get("food");
        this.resources.reputation += resChange.get("reputation");
        this.resources.technology += resChange.get("technology");
        this.resources.army += resChange.get("army");
    }

    public String getNextEventText() {
        this.eventManager.startNewEvent();
        return this.eventManager.getCurrentEventText();
    }
}

class Resources {
    int people, food, army, gold, reputation, technology;

    public Resources() {
        this.people = 100;
        this.army = 100;
        this.food = 100;
        this.gold = 1000;
        this.reputation = 50;
        this.technology = 0;
    }
}
