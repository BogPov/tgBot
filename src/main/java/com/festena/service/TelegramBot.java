package com.festena.service;

import com.festena.config.BotConfig;
import com.festena.manager.EnergyManager;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final private BotConfig config;
    final private BotService service;
    EnergyManager energyManager;

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    private static final String HELP_BTN_TXT = "\u2753 Помощь";
    private static final String RES_BTN_TXT = "\uD83D\uDCB0 Мои ресурсы";
    private static final String TOP_BTN_TXT = "\uD83C\uDFC6 Топ игроков";
    private static final String LORE_BTN_TXT = "\uD83D\uDCD6 Лор игры";
    private static final String RUN_GAME_BTN_TXT = "\uD83C\uDFAE Играть";

    public TelegramBot(BotConfig config, BotService service, EnergyManager energyManager){
        this.config = config;
        this.service = service;
        this.energyManager = energyManager;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Message msg = new Message();

            msg.setText(callbackQuery.getData());
            Chat chat = new Chat();
            chat.setId(callbackQuery.getMessage().getChatId());
            Long chatId = callbackQuery.getMessage().getChatId();
            msg.setChat(chat);
            msg.setFrom(callbackQuery.getFrom());

            String serviceResponse = service.processMessage(msg);
            sendTextMessage(chatId, serviceResponse);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            User user = update.getMessage().getFrom();
            Long userId = user.getId();
            Long chatId = update.getMessage().getChatId();
            Message msg = update.getMessage();
            //обработка reply кнопок
            if (!Character.isLetter(messageText.charAt(0))){
                switch (messageText){
                    case HELP_BTN_TXT -> msg.setText(BotService.SHOW_HELP_COMMAND);
                    case LORE_BTN_TXT -> msg.setText(BotService.SHOW_LORE_COMMAND);
                    case RES_BTN_TXT -> msg.setText(BotService.SHOW_PLAYER_RESOURCES_COMMAND);
                    case TOP_BTN_TXT -> msg.setText(BotService.SHOW_LEADERBOARD_COMMAND);
                    case RUN_GAME_BTN_TXT -> msg.setText(BotService.RUN_GAME_COMMAND);
                }
                if (messageText.contains(EnergyManager.ENERGY_SIGN)){
                    msg.setText(BotService.SHOW_ENERGY_MESSAGE_COMMAND);
                }
            }
            String serviceResponse = service.processMessage(update.getMessage());
            if (!serviceResponse.isEmpty()){
                sendTextMessage(chatId, serviceResponse);
            }
        }
    }

    public void sendTextMessage(Long chatId, String text) {
        text = processSpecialSymbols(text);
        try {
            if (!text.contains(BotService.STRING_DIVIDOR)) {
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

    public void notifyAllUsers(String notification){
        List<Long> allChatIds = service.getAllUsersChatId();
        for (Long chatId : allChatIds){
            sendTextMessage(chatId, notification);
        }
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
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        if (text.startsWith(BotService.NEED_ANSWER_BTNS_SYMBOL)
                || text.endsWith(BotService.NEED_ANSWER_BTNS_SYMBOL)){
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(InlineKeyboardButton.builder().text("A").callbackData("a").build());
            row1.add(InlineKeyboardButton.builder().text("B").callbackData("b").build());

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(InlineKeyboardButton.builder().text("C").callbackData("c").build());
            row2.add(InlineKeyboardButton.builder().text("D").callbackData("d").build());

            rows.add(row1);
            rows.add(row2);
            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            text = text.replace(BotService.NEED_ANSWER_BTNS_SYMBOL, "");
        } else if (text.contains(BotService.HAVE_TO_BUY_ENERGY_SYMBOL)){
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(InlineKeyboardButton.builder().text("Купить за " + BotService.ENERGY_COST + "\uD83D\uDCB0").
                    callbackData(BotService.BUY_ENERGY_COMMAND).build());
            rows.add(row1);
            markup.setKeyboard(rows);
            msg.setReplyMarkup(markup);
            text = text.replace(BotService.HAVE_TO_BUY_ENERGY_SYMBOL, "");
        }else{
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);
            keyboard.setOneTimeKeyboard(false);

            List<KeyboardRow> buttons = new ArrayList<>();

            switch (service.getUserState(chatId)) {
                case BotService.DIDNT_STARTED_STATE -> {
                    KeyboardRow row = new KeyboardRow();
                    row.add(BotService.BOT_START_COMMAND);
                    buttons.add(row);
                }
                case BotService.STARTED_BUT_DONT_PLAY_STATE -> {
                    KeyboardRow row1 = new KeyboardRow();
                    row1.add(HELP_BTN_TXT);
                    row1.add(RUN_GAME_BTN_TXT);
                    row1.add(RES_BTN_TXT);
                    KeyboardRow row2 = new KeyboardRow();
                    row2.add(LORE_BTN_TXT);
                    row2.add(energyManager.getPlayerEnergy(chatId) + EnergyManager.ENERGY_SIGN);
                    row2.add(TOP_BTN_TXT);
                    buttons.add(row1);
                    buttons.add(row2);
                }
                case BotService.IS_PLAYING_STATE_STATE -> {
                    KeyboardRow row1 = new KeyboardRow();
                    row1.add(HELP_BTN_TXT);
                    row1.add(RES_BTN_TXT);
                    KeyboardRow row2 = new KeyboardRow();
                    row2.add(LORE_BTN_TXT);
                    row2.add(TOP_BTN_TXT);
                    row2.add(energyManager.getPlayerEnergy(chatId) + EnergyManager.ENERGY_SIGN);
                    buttons.add(row1);
                    buttons.add(row2);
                }
            }
            keyboard.setKeyboard(buttons);
            msg.setReplyMarkup(keyboard);
        }
        msg.setText(text);
        return msg;
    }

    private ArrayList<String> messageDivider(String text){
        ArrayList<String> messages = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            String[] parts = text.split(BotService.STRING_DIVIDOR);
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
