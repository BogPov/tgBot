package com.festena.service;

import com.festena.Session.UserSession;
import com.festena.manager.TextManager;
import com.festena.manager.UserSessionManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BotService {
    // комманды
    final private static String COMMAND_SYMBOL = "/";
    final public static String BOT_START_COMMAND = "/start";
    final public static String RUN_GAME_COMMAND = "/play";
    final public static String SHOW_LORE_COMMAND = "/lore";
    final public static String SHOW_HELP_COMMAND = "/help";
    final public static String SHOW_PLAYER_RESOURCES_COMMAND = "/res";
    final public static String SHOW_LEADERBOARD_COMMAND = "/top";
    final public static String UNKNOWN_COMMAND_MESSAGE = "бро, я такой команды не знаю. Напиши /help";

    // ключи для TextManager
    final private static String WELCOME_MESSAGE = "welcome_message";
    final private static String LORE_MESSAGE = "lore";
    final private static String HELP_MESSAGE = "help";

    // Предупреждения
    final private static String START_BOT_FIRST_WARNING = "Перед тем как отправлять сообщения запусти бота!: /start";
    final private static String CORRECT_ANSWER_WARNING = "Ответ на событие должен быть написан одной латинцкой буквой!";
    final private static String START_EVENT_FIRST_WARNING = "Перед тем как писать сообщения, начни игру: /play";
    final private static String START_COMMAND_ABUSE_MESSAGE = "Бро, ты уже запустил бота!";
    final private static String EMPTY_STRING = "";

    // спец символы
    final public static String STRING_DIVIDOR = "\n\n";
    final public static String NEED_ANSWER_BTNS_SYMBOL = "@@@";

    // Строки с состояниями игры
    final public static String DIDNT_STARTED_STATE = "DIDNT_STARTED";
    final public static String IS_PLAYING_STATE_STATE = "IS_PLAYING";
    final public static String STARTED_BUT_DONT_PLAY_STATE = "STARTED_BUT_DONT_PLAY";

    final private UserSessionManager userSessionManager;
    final private TextManager textManager;
    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    public BotService(UserSessionManager userSessionManager, TextManager textManager) {
        this.userSessionManager = userSessionManager;
        this.textManager = textManager;
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
                return session.processPlayerAnswer(messageText) + NEED_ANSWER_BTNS_SYMBOL;
            } else {
                return CORRECT_ANSWER_WARNING + STRING_DIVIDOR + NEED_ANSWER_BTNS_SYMBOL +  session.getCurrentEventText();
            }
        } else {
            return START_EVENT_FIRST_WARNING;
        }
    }

    public String getUserState(Long chatId){
        if (userSessionManager.isSessionExist(chatId)){
            UserSession session = userSessionManager.getUserSession(chatId);
            if (session.hasCurrentEvent()){
                return IS_PLAYING_STATE_STATE;
            } else{
                return STARTED_BUT_DONT_PLAY_STATE;
            }
        } else{
            return DIDNT_STARTED_STATE;
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
                    return NEED_ANSWER_BTNS_SYMBOL + session.getNextEventText();
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
        Map<Long, UserSession> sessions = userSessionManager.getAllSessions();

        Map<Long, Integer> chatIdsWithGold = new HashMap<>();
        for (Map.Entry<Long, UserSession> entry : sessions.entrySet()) {
            chatIdsWithGold.put(entry.getKey(), entry.getValue().getAmountOfGold());
        }
        // Сортировка по убыванию золота
        List<Map.Entry<Long, Integer>> sortedByGold = chatIdsWithGold.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .toList();

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
