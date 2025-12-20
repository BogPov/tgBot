package com.festena.manager;

import com.festena.databases.PlayerEnergyDB;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnergyManager {
    private final PlayerEnergyDB playerEnergyDB;

    public static final int ENERGY_PER_HOUR = 10;
    public static final int MAX_ENERGY = 100;
    public static final String ENERGY_SIGN = "⚡";

    public EnergyManager(PlayerEnergyDB playerEnergyDB) {
        this.playerEnergyDB = playerEnergyDB;
    }

    public void addEnergyToPlayer(long chatId, int amountOfEnergy) {
        int current = playerEnergyDB.getPlayerEnergy(chatId);
        if (current >= MAX_ENERGY) {
            return;
        }

        int toAdd = Math.min(amountOfEnergy, MAX_ENERGY - current);

        playerEnergyDB.addEnergyToPlayer(chatId, toAdd);
    }

    public Integer getPlayerEnergy(Long chatId){
        return playerEnergyDB.getPlayerEnergy(chatId);
    }

    public boolean couldPlayerMakeATerm(long chatId){
        return playerEnergyDB.getPlayerEnergy(chatId) > 0;
    }

    public void regenerateEnergyForAllPlayers() {
        List<Long> players = playerEnergyDB.getAllPlayers();

        for (Long chatId : players) {
            addEnergyToPlayer(chatId, ENERGY_PER_HOUR);
        }
    }

    public void addNewPlayer(Long chatId){
        playerEnergyDB.addPlayerToDB(chatId);
    }
}
