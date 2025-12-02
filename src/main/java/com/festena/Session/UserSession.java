package com.festena.Session;

import com.festena.manager.EventManager;
import com.festena.service.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class UserSession {
    private final Long chatId;
    private final Long userId;

    private Resources resources;
    private EventManager eventManager = new EventManager();

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    private static final String PEOPLE_ICON = "👥Население ";
    private static final String GOLD_ICON = " | 💰Золото ";
    private static final String FOOD_ICON = " | 🌾Продовольствие ";
    private static final String ARMY_ICON = " | ⚔ Армия ";
    private static final String TECHNOLOGY_ICON = " | ⚙ Технологии ";
    private static final String REPUTATION_ICON = " | 🏅Репутация ";
    final private static String STRING_DIVIDOR = "\n\n";

    public UserSession(Long chatId, Long userId){
        this.chatId = chatId;
        this.userId = userId;
        this.resources = new Resources();
        log.info("Новая сессия c айди пользователя {} создана", userId);
    }

    public String getResForTab() {
        return PEOPLE_ICON + resources.getPeople()
                + GOLD_ICON + resources.getGold()
                + FOOD_ICON + resources.getFood()
                + ARMY_ICON + resources.getArmy()
                + TECHNOLOGY_ICON + resources.getTechnology()
                + REPUTATION_ICON + resources.getReputation();
    }

    public String getChangedResForTab(HashMap<String, Integer> resourceChange){
        StringBuilder result = new StringBuilder();

        String[][] resources = {
                {"people", PEOPLE_ICON},
                {"gold", GOLD_ICON},
                {"food", FOOD_ICON},
                {"army", ARMY_ICON},
                {"technology", TECHNOLOGY_ICON},
                {"reputation", REPUTATION_ICON}
        };
        for (String[] resource : resources) {
            String key = resource[0];
            String icon = resource[1];
            Integer value = resourceChange.get(key);
            if (value != null && value != 0) {
                result.append(icon).append(value > 0 ? "+" : "").append(value);
            }
        }
        return result.toString();
    }

    public String processPlayerAnswer(String playerResponse) {
        HashMap<String, Integer> resourceChanges = eventManager.getResourceChanges(playerResponse);
        resources.addGold(resourceChanges.get("gold"));
        resources.addPeople(resourceChanges.get("people"));
        resources.addFood(resourceChanges.get("food"));
        resources.addReputation(resourceChanges.get("reputation"));
        resources.addTechnology(resourceChanges.get("technology"));
        resources.addArmy(resourceChanges.get("army"));
        return getChangedResForTab(resourceChanges) + STRING_DIVIDOR + getNextEventText();
    }

    public boolean hasCurrentEvent(){
        return !eventManager.getCurrentEventText().isEmpty();
    }

    public Integer getAmountOfGold(){
        return resources.getGold();
    }

    public String getCurrentEventText(){
        return eventManager.getCurrentEventText();
    }

    public String getNextEventText() {
        eventManager.startNewEvent();
        return eventManager.getCurrentEventText();
    }
}

class Resources {
    private int people;
    private int food;
    private int army;
    private int gold;
    private int reputation;
    private int technology;

    public Resources() {
        this.people = 100;
        this.army = 100;
        this.food = 100;
        this.gold = 1000;
        this.reputation = 50;
        this.technology = 0;
    }

    public int getPeople() {
        return people;
    }

    public int getFood() {
        return food;
    }

    public int getArmy() {
        return army;
    }

    public int getGold() {
        return gold;
    }

    public int getReputation() {
        return reputation;
    }

    public int getTechnology() {
        return technology;
    }

    public void addPeople(int delta) {
        this.people += delta;
    }

    public void addFood(int delta) {
        this.food += delta;
    }

    public void addArmy(int delta) {
        this.army += delta;
    }

    public void addGold(int delta) {
        this.gold += delta;
    }

    public void addReputation(int delta) {
        this.reputation += delta;
    }

    public void addTechnology(int delta) {
        this.technology += delta;
    }
}