package com.festena.tgBot.service;

import com.festena.tgBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final private BotConfig config;
    final private BotService service;

    final private static String STRING_DIVIDOR = "\n\n";
    final private static String CONVERT_INT_TO_USERNAME_SYMBOL = "!!";

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    public TelegramBot(BotConfig config){
        this.config = config;
        this.service = new BotService();
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            User user = update.getMessage().getFrom();
            Long userId = user.getId();
            Long chatId = update.getMessage().getChatId();

            String serviceResponse = service.processMessage(update.getMessage());
            if (!serviceResponse.isEmpty()){
                sendTextMessage(chatId, serviceResponse);
            }
        }
    }
    public void sendTextMessage(Long chatId, String text) {
        text = processSpecialSymbols(text);
        try {
            if (!text.contains(STRING_DIVIDOR)) {
                sendSingleMessage(chatId, text);
            } else {
                sendSplitMessages(chatId, text);
            }
            log.info("Сообщение успешно отправлено в чат: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения в чат: {}", chatId, e);
        }
    }

    private void sendSingleMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage message = createBaseMessage(chatId, text);
        execute(message);
    }

    private String processSpecialSymbols(String text){
        if (text == null) return null;

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("!!(.*?)!!").matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String chatIdStr = matcher.group(1);
            try {
                Long chatId = Long.parseLong(chatIdStr);
                String userName = getUserName(chatId);
                matcher.appendReplacement(result, userName);
            } catch (NumberFormatException e) {
                // Если не число, оставляем как есть
                matcher.appendReplacement(result, "!!" + chatIdStr + "!!");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private void sendSplitMessages(Long chatId, String text) throws TelegramApiException {
        List<String> messageParts = messageDivider(text);
        for (String part : messageParts) {
            SendMessage message = createBaseMessage(chatId, part);
            execute(message);
        }
    }

    private SendMessage createBaseMessage(Long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
    }

    private ArrayList<String> messageDivider(String text){
        ArrayList<String> messages = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            String[] parts = text.split("\n\n");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    messages.add(part.trim());
                }
            }
        }
        return messages;

    }
    private String getUserName(Long chatId) {
        try {
            GetChat getChat = new GetChat(chatId.toString());
            Chat chat = execute(getChat);

            return chat.getUserName() != null ? "@" + chat.getUserName()
                    : chat.getFirstName() != null ? chat.getFirstName()
                    : "Unknown";
        } catch (TelegramApiException e) {
            return "Unknown";
        }
    }

    @Override
    public String getBotToken(){
        return config.getBotToken();
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
}
