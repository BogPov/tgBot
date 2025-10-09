package com.festena.bot;

import javax.security.auth.callback.TextInputCallback;

import com.festena.bot.Console;
import com.festena.bot.Game;
import com.festena.bot.TextManager;

public class App 
{
    private static Console cons = new Console();
    private boolean runFlag = true;

    public static void main( String[] args )
    {
        App app = new App();
        cons.clear();
        cons.out(TextManager.getText("welcome_message"));
        while (true){
            String ans = cons.input("");
            app.processCommand(ans);    
        }
    } 

    private void start(){
        Game game = new Game();
        while (this.runFlag){
            cons.clear();
            cons.out(game.getResForTab());
            cons.out("\n" + "\n" + "\n");
                        
            cons.out(game.getNextEventText());
            String player_response = cons.input("\n Выбирите вариант ответа: ");
            if (player_response.startsWith("/")){
                this.processCommand(player_response);
                cons.input("\n Нажмите enter для продолжения... ");
                continue;
            }
            
            game.proccesPlAnswer(player_response.toUpperCase());
            cons.input("\n Нажмите enter для продолжения...");
            
        }
    }
    private void processCommand(String response){
            switch (response) {
                case "/help": cons.out(TextManager.getText("help")); break;
                case "/stop": System.exit(0); break;
                case "/lore": cons.out(TextManager.getText("lore")); break;
                case "/start": this.start();
                default: cons.out("Такой команды не существует, бро");
            }   
        }
    
}
