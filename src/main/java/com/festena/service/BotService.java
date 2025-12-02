package com.festena.tgBot.service;

import com.festena.tgBot.Session.UserSession;
import com.festena.tgBot.manager.TextManager;
import com.festena.tgBot.manager.UserSessionManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BotService {
    final private static String COMMAND_SYMBOL = "/";
    final private static String BOT_START_COMMAND = "/start";
    final private static String RUN_GAME_COMMAND = "/play";
    final private static String SHOW_LORE_COMMAND = "/lore";
    final private static String SHOW_HELP_COMMAND = "/help";
    final private static String SHOW_PLAYER_RESOURCES_COMMAND = "/res";
    final private static String SHOW_LEADERBOARD_COMMAND = "/top";
    final private static String UNKNOWN_COMMAND_MESSAGE = "бро, я такой команды не знаю. Напиши /help";

    final private static String WELCOME_MESSAGE = "welcome_message";
    final private static String LORE_MESSAGE = "lore";
    final private static String HELP_MESSAGE = "help";
    final private static String START_BOT_FIRST_WARNING = "Перед тем как отправлять сообщения запусти бота!: /start";
    final private static String CORRECT_ANSWER_WARNING = "Ответ на событие должен быть написан одной латинцкой буквой!";
    final private static String START_EVENT_FIRST_WARNING = "Перед тем как писать сообщения, начни игру: /play";

    final private static String START_COMMAND_ABUSE_MESSAGE = "Бро, ты уже запустил бота!";
    final private static String EMPTY_STRING = "";
    // при отправке сообщения строка обрабатывается на ключение этой строки, и разбивает сообщение на несколько
    final private static String STRING_DIVIDOR = "\n\n";

    final private UserSessionManager userSessionManager;
    final private TextManager textManager;
    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    public BotService() {
        this.userSessionManager = new UserSessionManager();
        this.textManager = new TextManager();
    }

    public String processMessage(Message message) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        if (isMessageCommand(messageText)) {
            return this.processCommand(message);
        }
        if (!userSessionManager.isSessionExist(chatId)) {
            return START_BOT_FIRST_WARNING;
        }

        UserSession session = userSessionManager.getUserSession(chatId);
        if (session.hasCurrentEvent()) {
            if (messageText.matches("(?i)[abcd]")) {
                return session.processPlayerAnswer(messageText);
            } else {
                return CORRECT_ANSWER_WARNING + STRING_DIVIDOR + session.getCurrentEventText();
            }
        } else {
            return START_EVENT_FIRST_WARNING;
        }
    }

    private String processCommand(Message message) {
        Long chatId = message.getChatId();
        String messageText = message.getText();

        // проверям запустил ли игрок бота перед тем как давать ему возможность вводить команды
        if (userSessionManager.isSessionExist(chatId)) {
            UserSession session = userSessionManager.getUserSession(chatId);
            switch (messageText) {
                case SHOW_HELP_COMMAND -> {
                    return textManager.getText(HELP_MESSAGE);
                }
                case SHOW_LORE_COMMAND -> {
                    return textManager.getText(LORE_MESSAGE);
                }
                case BOT_START_COMMAND -> {
                    return START_COMMAND_ABUSE_MESSAGE;
                }
                case RUN_GAME_COMMAND -> {
                    return session.getNextEventText();
                }
                case SHOW_PLAYER_RESOURCES_COMMAND -> {
                    return session.getResForTab();
                }
                case SHOW_LEADERBOARD_COMMAND -> {
                    return this.getOnlinePlayersLeaderboard();
                }
                default -> {
                    return UNKNOWN_COMMAND_MESSAGE;
                }
            }
        } else {
            if (messageText.equals(BOT_START_COMMAND)) {
                userSessionManager.addSession(chatId, message.getFrom().getId());
                return textManager.getText(WELCOME_MESSAGE);
            }
        }
        return EMPTY_STRING;
    }

    String getOnlinePlayersLeaderboard() {
        HashMap<Long, UserSession> sessions = userSessionManager.getAllSessions();

        HashMap<Long, Integer> chatIdsWithGold = new HashMap<>();
        for (Map.Entry<Long, UserSession> entry : sessions.entrySet()) {
            chatIdsWithGold.put(entry.getKey(), entry.getValue().getAmountOfGold());
        }
        // Сортировка по убыванию золота
        List<Map.Entry<Long, Integer>> sortedByGold = chatIdsWithGold.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();
        result.append("ТОП ИГРОКОВ ПО ЗОЛОТУ\n");
        for (Map.Entry<Long, Integer> entry : sortedByGold) {
            result.append("!!")
                    .append(entry.getKey())
                    .append("!! - ")
                    .append(entry.getValue())
                    .append("\n");
        }
        return result.toString();
    }

    private boolean isMessageCommand(String message) {
        return message.startsWith(COMMAND_SYMBOL);
    }
}
