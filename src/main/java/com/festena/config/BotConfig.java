package com.festena.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    public String getBotName(){return botName;}

    public String getBotToken(){return botToken;}
}
