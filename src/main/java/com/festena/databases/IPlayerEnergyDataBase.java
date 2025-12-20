package com.festena.databases;

import java.util.List;

public interface IPlayerEnergyDataBase {

    List<Long> getAllPlayers();

    int getPlayerEnergy(long chatId);

    void addEnergyToPlayer(long chatId, int delta);

    void addPlayerToDB(long chatId);
}
