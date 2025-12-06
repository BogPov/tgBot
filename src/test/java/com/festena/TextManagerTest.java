package com.festena;

import com.festena.manager.TextManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextManagerTest {

    private TextManager textManager;

    @BeforeEach
    void setUp() {
        textManager = new TextManager();
    }

    @Test
    void constructorLoadsYamlSuccessfully() {
        assertNotNull(textManager);
        // Проверяем, что какой-либо известный текст загружен
        assertNotNull(textManager.getText("welcome_message"));
        assertTrue(textManager.getText("welcome_message").contains("Добро пожаловать"));
    }

    @Test
    void getTextReturnsCorrectText() {
        String expectedWelcomeMessage = "Добро пожаловать в текстовую RPG игру Kingdom\n" +
                "Тебе предстоит взять на себя ношу короля и спасти свое королевство\n" +
                "Каждое твое решение отразится на будущем\n" +
                "/help - список основной информации по игровым механикам и командам\n" +
                "/lore - лор игры\n" +
                "/res - посмотреть количество ресурсов\n" +
                "/top - топ игроков по золоту\n" +
                "/play - начать игру\n";
        String actualWelcomeMessage = textManager.getText("welcome_message");
        assertEquals(expectedWelcomeMessage, actualWelcomeMessage);

        //help
        String expectedHelpStartsWith = "В игре присутствует 6 видов ресурсов - золото, народ, армия, технологии, репутация и продовольствие";
        String actualHelp = textManager.getText("help");
        assertTrue(actualHelp.startsWith(expectedHelpStartsWith));
    }
}
