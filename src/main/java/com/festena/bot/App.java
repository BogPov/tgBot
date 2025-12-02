package com.festena.bot;

public class App {
    private Console console;
    private TextManager textManager;

    private static final String WELCOME_MESSAGE = "welcome_message";
    private static final String HELP_COMMAND = "/help";
    private static final String STOP_COMMAND = "/stop";
    private static final String LORE_COMMAND = "/lore";
    private static final String START_COMMAND = "/start";
    private static final String INPUT_PROMPT = "\n Выбирите вариант ответа: ";
    private static final String CONTINUE_PROMPT = "\n Нажмите enter для продолжения... ";
    private static final String UNKNOWN_COMMAND_MESSAGE = "Такой команды не существует, бро";

    public App() {
        this.console = new Console();
        this.textManager = new TextManager();
    }

    public static void main(String[] args) {
        App app = new App();
        app.initializeAndRun();
    }

    public void initializeAndRun() {
        console.clear();
        console.out(textManager.getText(WELCOME_MESSAGE));
        while (true) {
            String answer = console.input("");
            processCommand(answer);
        }
    }

    protected void start() {
        Game game = new Game();
        while (true) {
            console.clear();
            console.out(game.getResForTab());
            console.out("\n\n\n");
            console.out(game.getNextEventText());
            String playerResponse = console.input(INPUT_PROMPT);
            if (playerResponse.startsWith("/")) {
                processCommand(playerResponse);
                console.input(CONTINUE_PROMPT);
                continue;
            }
            game.processPlayerAnswer(playerResponse.toUpperCase());
            console.input(CONTINUE_PROMPT);
        }
    }

    protected void processCommand(String response) {
        switch (response) {
            case HELP_COMMAND:
                console.out(textManager.getText("help"));
                break;
            case STOP_COMMAND:
                System.exit(0);
                break;
            case LORE_COMMAND:
                console.out(textManager.getText("lore"));
                break;
            case START_COMMAND:
                start();
                break;
            default:
                console.out(UNKNOWN_COMMAND_MESSAGE);
        }
    }
}
