package com.festena.fakeDBs;

import com.festena.databases.IPlayerEnergyDataBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakePlayerEnergyDB implements IPlayerEnergyDataBase {
    Map<Long, Integer> table;

    public static final int DEFAULT_ENERGY = 10;

    public FakePlayerEnergyDB(){
        this.table = new HashMap<Long, Integer>();
    }
    @Override
    public List<Long> getAllPlayers() {
        return table.keySet().stream().toList();
    }

    @Override
    public int getPlayerEnergy(long chatId) {
        return table.get(chatId);
    }

    @Override
    public void addEnergyToPlayer(long chatId, int delta) {
        int value = table.get(chatId);
        table.replace(chatId, value + delta);
    }

    @Override
    public void addPlayerToDB(long chatId) {
        table.put(chatId, DEFAULT_ENERGY);
    }
}
