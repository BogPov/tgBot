package com.festena.bot;

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
        // берем файл "/events.json" из папки resources
        try (InputStream inputStream = getClass().getResourceAsStream("/events.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Resource '/events.json' not found in classpath");
            }
            // Читаем как List<Event>
            List<Event> events = mapper.readValue(inputStream, new TypeReference<List<Event>>() {});
            if (events != null) {
                eventStorage.addAll(events);
            }
        } catch (IOException e) {
            // Логгируй или пробрасывай дальше — зависит от архитектуры
            throw new RuntimeException("Failed to load events.json", e);
        }
    }

    public void startNewEvent() {
        if (eventStorage.isEmpty()) {
            throw new IllegalStateException("No events loaded");
        }
        this.currentEvent = eventStorage.get(random.nextInt(eventStorage.size()));
    }

    public String getCurrentEventText() {
        if (currentEvent == null) return "No current event. Call startNewEvent() first.";
        StringBuilder sb = new StringBuilder(currentEvent.legend).append("\n");
        if (currentEvent.options != null) {
            for (Option ans : currentEvent.options) {
                sb.append(ans.id).append(". ").append(ans.text).append("\n");
            }
        }
        return sb.toString();
    }

    public HashMap<String, Integer> getResChange(String id){
        HashMap<String, Integer> resChange = new HashMap<>();
        resChange.put("gold", 0);
        resChange.put("people", 0);
        resChange.put("food", 0);
        resChange.put("army", 0);
        resChange.put("technology", 0);
        resChange.put("reputation", 0);

        if (this.currentEvent != null && this.currentEvent.options != null) {
            for (Option opt : this.currentEvent.options){
                if (opt.id.equalsIgnoreCase(id.trim())){
                    resChange.put("gold", opt.gold);
                    resChange.put("people", opt.people);
                    resChange.put("food", opt.food);
                    resChange.put("army", opt.army);
                    resChange.put("technology", opt.technology);
                    resChange.put("reputation", opt.reputation);
                    return resChange;
                }
            }
        }
        return resChange;
    }
}

class Event {
    public String legend;
    public List<Option> options;

    // пустой конструктор нужен Jackson'у
    public Event() {}

}

class Option {
    public String id;
    public String text;
    public int gold, people, food, army, technology, reputation;

    public Option() {}

    @JsonProperty("effects")
    private void unpackEffects(Map<String, Integer> effects) {
        if (effects == null) return;
        this.gold = effects.getOrDefault("gold", 0);
        this.people = effects.getOrDefault("people", 0);
        this.food = effects.getOrDefault("food", 0);
        this.army = effects.getOrDefault("army", 0);
        this.technology = effects.getOrDefault("technology", 0);
        this.reputation = effects.getOrDefault("reputation", 0);
    }
}
