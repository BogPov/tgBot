package com.festena.Session;

import com.festena.databases.PlayersResDB;
import com.festena.manager.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private final Long chatId;
    private final Long userId;

    private final Resources resources;
    private final EventManager eventManager;

    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    private static final String PEOPLE_ICON = "👥Население ";
    private static final String GOLD_ICON = " | 💰Золото ";
    private static final String FOOD_ICON = " | 🌾Продовольствие ";
    private static final String ARMY_ICON = " | ⚔ Армия ";
    private static final String TECHNOLOGY_ICON = " | ⚙ Технологии ";
    private static final String REPUTATION_ICON = " | 🏅Репутация ";
    final private static String STRING_DIVIDOR = "\n\n";

    public UserSession(Long chatId, Long userId){
        this(chatId, userId, new EventManager());
    }

    public UserSession(Long chatId, Long userId, EventManager eventManager){
        this.chatId = chatId;
        this.userId = userId;
        this.resources = new Resources();
        this.eventManager = eventManager;
        log.info("Новая сессия c айди пользователя {} создана", userId);
    }

    public Long getChatId() {
        return chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public Resources getResources() { // Для доступа к ресурсам в тестах
        return resources;
    }

    public String getResForTab() {
        return PEOPLE_ICON + resources.getPeople()
                + GOLD_ICON + resources.getGold()
                + FOOD_ICON + resources.getFood()
                + ARMY_ICON + resources.getArmy()
                + TECHNOLOGY_ICON + resources.getTechnology()
                + REPUTATION_ICON + resources.getReputation();
    }

    public String getChangedResForTab(Map<String, Integer> resourceChange){
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
        Map<String, Integer> resourceChanges = eventManager.getResourceChanges(playerResponse);
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

    public void addGold(Integer amount){
        resources.addGold(amount);
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

    public void updateRes(Map<String, Integer> newRes){
        this.resources.setArmy(newRes.get(PlayersResDB.ARMY_KEY));
        this.resources.setGold(newRes.get(PlayersResDB.GOLD_KEY));
        this.resources.setPeople(newRes.get(PlayersResDB.PEOPLE_KEY));
        this.resources.setTechnology(newRes.get(PlayersResDB.TECHNOLOGY_KEY));
        this.resources.setReputation(newRes.get(PlayersResDB.REPUTATION_KEY));
        this.resources.setFood(newRes.get(PlayersResDB.FOOD_KEY));
    }
}