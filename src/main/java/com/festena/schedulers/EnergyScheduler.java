package com.festena.schedulers;

import com.festena.manager.EnergyManager;
import com.festena.service.TelegramBot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EnergyScheduler {

    private final EnergyManager energyManager;
    private final TelegramBot tgBot;

    public static final String ENERGY_REGENERATION_NOTIFICATION = "Запасы энергии пополнены +" +
            EnergyManager.ENERGY_PER_HOUR + EnergyManager.ENERGY_SIGN;

    public EnergyScheduler(EnergyManager energyManager, TelegramBot tgBot) {
        this.energyManager = energyManager;
        this.tgBot = tgBot;
    }

    @Scheduled(cron = "0 0 * * * *") // каждый час
    public void hourlyEnergyRegen() {
        energyManager.regenerateEnergyForAllPlayers();
        tgBot.notifyAllUsers(ENERGY_REGENERATION_NOTIFICATION);
    }
}
