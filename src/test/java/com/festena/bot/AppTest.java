package com.festena.bot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AppTest {
    @Mock
    private Console mockConsole;

    @Mock
    private TextManager mockTextManager;

    @InjectMocks
    private App appToTest;

    @Test
    public void testProcessCommandHelp() {
        when(mockTextManager.getText("help")).thenReturn("help text");
        appToTest.processCommand("/help");
        verify(mockConsole, times(1)).out("help text");
        verifyNoMoreInteractions(mockConsole);
    }

    @Test
    public void testProcessCommandLore() {
        when(mockTextManager.getText("lore")).thenReturn("lore text");
        appToTest.processCommand("/lore");
        verify(mockConsole, times(1)).out("lore text");
        verifyNoMoreInteractions(mockConsole);
    }

    @Test
    public void testProcessCommandUnknownCommandDefaultCase() {
        appToTest.processCommand("/some_non_existent_command");
        verify(mockConsole, times(1)).out("Такой команды не существует, бро");
        verifyNoMoreInteractions(mockConsole);
    }
}
