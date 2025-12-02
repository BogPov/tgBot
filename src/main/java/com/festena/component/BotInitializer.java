package com.festena.component;

import com.festena.service.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BotInitializer {

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TelegramBotsApi telegramBotsApi;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        try {
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            // Handle exception properly
            e.printStackTrace();
            throw new RuntimeException("Failed to register bot", e);
        }
    }
}
