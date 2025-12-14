package com.festena.Session;

import com.festena.manager.DataBaseManager;
import com.festena.manager.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final DataBaseManager dbManager;

    public UserSession(Long chatId, Long userId){
        this(chatId, userId, new EventManager());
    }

    public UserSession(Long chatId, Long userId, EventManager eventManager){
        this.chatId = chatId;
        this.userId = userId;
        this.resources = new Resources();
        this.dbManager = new DataBaseManager();
        if (dbManager.isPlayerExists(chatId)){
            Map<String, Integer> dbRes = dbManager.getPlayerData(chatId);
            this.resources.setArmy(dbRes.get(DataBaseManager.ARMY_KEY));
            this.resources.setGold(dbRes.get(DataBaseManager.GOLD_KEY));
            this.resources.setPeople(dbRes.get(DataBaseManager.PEOPLE_KEY));
            this.resources.setTechnology(dbRes.get(DataBaseManager.TECHNOLOGY_KEY));
            this.resources.setReputation(dbRes.get(DataBaseManager.REPUTATION_KEY));
            this.resources.setFood(dbRes.get(DataBaseManager.FOOD_KEY));
        } else{
            dbManager.addPlayer(chatId);
            dbManager.updatePlayer(chatId, resources.getGold(), resources.getPeople(), resources.getReputation(),
                    resources.getFood(), resources.getArmy(), resources.getTechnology());
        }
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

        dbManager.updatePlayer(chatId, resources.getGold(), resources.getPeople(), resources.getReputation(),
                resources.getFood(), resources.getArmy(), resources.getTechnology());

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