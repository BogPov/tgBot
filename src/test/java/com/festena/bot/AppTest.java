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

    @InjectMocks
    private App appToTest;

    @Test
    public void testProcessCommand_Help() {
        appToTest.processCommand("/help");
        verify(mockConsole, times(1)).out(TextManager.getText("help"));
        verifyNoMoreInteractions(mockConsole);
    }

    @Test
    public void testProcessCommand_Lore() {
        appToTest.processCommand("/lore");
        verify(mockConsole, times(1)).out(TextManager.getText("lore"));
        verifyNoMoreInteractions(mockConsole);
    }

    @Test
    public void testProcessCommand_UnknownCommand_DefaultCase() {
        appToTest.processCommand("/some_non_existent_command");
        verify(mockConsole, times(1)).out("Такой команды не существует, бро");
        verifyNoMoreInteractions(mockConsole);
    }
}
