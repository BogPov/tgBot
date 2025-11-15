package com.festena.bot;

public class App {
    private Console cons;
    private boolean runFlag = true;

    public App(Console cons) {
        this.cons = cons;
    }

    public static void main(String[] args) {
        Console realConsole = new Console();
        App app = new App(realConsole);
        realConsole.clear();
        realConsole.out(TextManager.getText("welcome_message"));
        while (true) {
            String ans = realConsole.input("");
            app.processCommand(ans);
        }
    }

    protected void start() {
        Game game = new Game();
        while (this.runFlag) {
            cons.clear();
            cons.out(game.getResForTab());
            cons.out("\n" + "\n" + "\n");
            cons.out(game.getNextEventText());
            String playerResponse = cons.input("\n Выбирите вариант ответа: ");
            if (playerResponse.startsWith("/")) {
                this.processCommand(playerResponse);
                cons.input("\n Нажмите enter для продолжения... ");
                continue;
            }
            game.processPlayerAnswer(playerResponse.toUpperCase());
            cons.input("\n Нажмите enter для продолжения...");
        }
    }

    protected void processCommand(String response) {
        switch (response) {
            case "/help":
                cons.out(TextManager.getText("help"));
                break;
            case "/stop":
                System.exit(0);
                break;
            case "/lore":
                cons.out(TextManager.getText("lore"));
                break;
            case "/start":
                this.start();
                break;
            default:
                cons.out("Такой команды не существует, бро");
        }
    }
}
