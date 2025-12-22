package com.festena.fakeDBs;

import com.festena.Session.Resources;
import com.festena.databases.IDataBase;
import com.festena.databases.PlayersResDB;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FakePlayerResDB implements IDataBase {

    private final Map<Long, Resources> table;

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            PlayersResDB.GOLD_KEY,
            PlayersResDB.PEOPLE_KEY,
            PlayersResDB.REPUTATION_KEY,
            PlayersResDB.FOOD_KEY,
            PlayersResDB.ARMY_KEY,
            PlayersResDB.TECHNOLOGY_KEY
    );

    public FakePlayerResDB() {
        this.table = new HashMap<>();
    }

    private String requireAllowedField(String field) {
        if (field == null || !ALLOWED_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Unsupported field");
        }
        return field;
    }

    private Resources requirePlayer(long chatId) {
        Resources res = table.get(chatId);
        if (res == null) {
            throw new IllegalStateException("Player not found");
        }
        return res;
    }

    @Override
    public boolean isPlayerExists(long chatId) {
        return table.containsKey(chatId);
    }

    @Override
    public void addPlayer(long chatId) {
        table.putIfAbsent(chatId, new Resources());
    }

    @Override
    public Map<String, Integer> getPlayerData(long chatId) {
        Resources res = requirePlayer(chatId);

        Map<String, Integer> data = new HashMap<>();
        data.put(PlayersResDB.GOLD_KEY, res.getGold());
        data.put(PlayersResDB.REPUTATION_KEY, res.getReputation());
        data.put(PlayersResDB.ARMY_KEY, res.getArmy());
        data.put(PlayersResDB.FOOD_KEY, res.getFood());
        data.put(PlayersResDB.PEOPLE_KEY, res.getPeople());
        data.put(PlayersResDB.TECHNOLOGY_KEY, res.getTechnology());
        return data;
    }

    @Override
    public int getPlayerValue(long chatId, String field) {
        String safeField = requireAllowedField(field);
        Resources res = requirePlayer(chatId);

        return switch (safeField) {
            case PlayersResDB.GOLD_KEY -> res.getGold();
            case PlayersResDB.PEOPLE_KEY -> res.getPeople();
            case PlayersResDB.REPUTATION_KEY -> res.getReputation();
            case PlayersResDB.FOOD_KEY -> res.getFood();
            case PlayersResDB.ARMY_KEY -> res.getArmy();
            case PlayersResDB.TECHNOLOGY_KEY -> res.getTechnology();
            default -> throw new IllegalArgumentException("Unsupported field");
        };
    }

    @Override
    public void updatePlayer(long chatId, int gold, int people, int respect, int food, int army, int technology) {
        Resources res = requirePlayer(chatId);
        res.setGold(gold);
        res.setPeople(people);
        res.setReputation(respect);
        res.setFood(food);
        res.setArmy(army);
        res.setTechnology(technology);
    }

    @Override
    public void updatePlayerField(long chatId, String field, int value) {
        String safeField = requireAllowedField(field);
        Resources res = requirePlayer(chatId);

        switch (safeField) {
            case PlayersResDB.GOLD_KEY -> res.setGold(value);
            case PlayersResDB.PEOPLE_KEY -> res.setPeople(value);
            case PlayersResDB.REPUTATION_KEY -> res.setReputation(value);
            case PlayersResDB.FOOD_KEY -> res.setFood(value);
            case PlayersResDB.ARMY_KEY -> res.setArmy(value);
            case PlayersResDB.TECHNOLOGY_KEY -> res.setTechnology(value);
            default -> throw new IllegalArgumentException("Unsupported field");
        }
    }

    @Override
    public Map<Long, Integer> getTopPlayers(int limit) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < limit; i++) {
            Long bestChatId = null;
            int bestGold = Integer.MIN_VALUE;
            for (Map.Entry<Long, Resources> e : table.entrySet()) {
                long chatId = e.getKey();
                if (result.containsKey(chatId)) continue;
                int gold = e.getValue().getGold();
                if (bestChatId == null || gold > bestGold) {
                    bestChatId = chatId;
                    bestGold = gold;
                }
            }
            if (bestChatId == null) break;
            result.put(bestChatId, bestGold);
        }

        return result;
    }
}
