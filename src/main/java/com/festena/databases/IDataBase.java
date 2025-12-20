package com.festena.databases;

import java.util.Map;

public interface IDataBase {

    boolean isPlayerExists(long chatId);

    void addPlayer(long chatId);

    Map<String, Integer> getPlayerData(long chatId);

    int getPlayerValue(long chatId, String field);

    void updatePlayer(long chatId,
                      int gold, int people, int respect,
                      int food, int army, int technology);

    void updatePlayerField(long chatId, String field, int value);

    Map<Long, Integer> getTopPlayers(int limit);
}