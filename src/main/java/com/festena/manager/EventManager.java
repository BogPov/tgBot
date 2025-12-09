package com.festena.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventManager {
    private List<Event> eventStorage = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();
    private Event currentEvent;
    private Random random = new Random();

    public EventManager() {
        try (InputStream inputStream = getClass().getResourceAsStream("/events.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Resource '/events.json' not found in classpath");
            }

            List<Event> events = mapper.readValue(inputStream, new TypeReference<List<Event>>() {
            });
            if (events != null) {
                eventStorage.addAll(events);
            }
        } catch (IOException e) {

            throw new RuntimeException("Failed to load events.json", e);
        }
    }

    public List<Event> getEventStorage() {
        return eventStorage;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event event) {
        this.currentEvent = event;
    }

    public void startNewEvent() {
        if (eventStorage.isEmpty()) {
            throw new IllegalStateException("No events loaded");
        }
        this.currentEvent = eventStorage.get(random.nextInt(eventStorage.size()));
    }

    public String getCurrentEventText() {
        if (currentEvent == null) return "";
        StringBuilder sb = new StringBuilder(currentEvent.getLegend()).append("\n");
        if (currentEvent.getOptions() != null) {
            for (Option ans : currentEvent.getOptions()) {
                sb.append(ans.getId()).append(". ").append(ans.getText()).append("\n");
            }
        }
        return sb.toString();
    }

    public Map<String, Integer> getResourceChanges(String id) {
        Map<String, Integer> resourceChange = new HashMap<>();
        resourceChange.put("gold", 0);
        resourceChange.put("people", 0);
        resourceChange.put("food", 0);
        resourceChange.put("army", 0);
        resourceChange.put("technology", 0);
        resourceChange.put("reputation", 0);

        if (this.currentEvent != null && this.currentEvent.getOptions() != null) {
            for (Option opt : this.currentEvent.getOptions()) {
                if (opt.getId().equalsIgnoreCase(id.trim())) {
                    resourceChange.put("gold", opt.getGold());
                    resourceChange.put("people", opt.getPeople());
                    resourceChange.put("food", opt.getFood());
                    resourceChange.put("army", opt.getArmy());
                    resourceChange.put("technology", opt.getTechnology());
                    resourceChange.put("reputation", opt.getReputation());
                    return resourceChange;
                }
            }
        }
        return resourceChange;
    }
}

class Event {
    private String legend;
    private List<Option> options;

    public Event(String legend, List<Option> options){
        this.legend = legend;
        this.options = options;
    }

    public void setLegend(String legend){
        this.legend = legend;
    }

    public Event(String legend) {
        this.legend = legend;
    }

    // пустой конструктор нужен Jackson'y
    public Event() {
    }

    public String getLegend() {
        return legend;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }
}

class Option {
    private String id;
    private String text;
    private int gold, people, food, army, technology, reputation;

    public Option() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getArmy() {
        return army;
    }

    public void setArmy(int army) {
        this.army = army;
    }

    public int getTechnology() {
        return technology;
    }

    public void setTechnology(int technology) {
        this.technology = technology;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    @JsonProperty("effects")
    void unpackEffects(Map<String, Integer> effects) {
        if (effects == null) return;
        this.gold = effects.getOrDefault("gold", 0);
        this.people = effects.getOrDefault("people", 0);
        this.food = effects.getOrDefault("food", 0);
        this.army = effects.getOrDefault("army", 0);
        this.technology = effects.getOrDefault("technology", 0);
        this.reputation = effects.getOrDefault("reputation", 0);
    }
}
