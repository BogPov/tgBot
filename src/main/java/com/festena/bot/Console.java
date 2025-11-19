package com.festena.bot;

import java.util.Scanner;

public class Console{
    private Scanner scanner = new Scanner(System.in);
    
    public void out(String message) {
        System.out.print(message);
    }
    
    public String input(String message) {
        System.out.print(message);
        String out = scanner.nextLine();
        return out;
    }

    public void clear() {
        System.out.print("\033[H\033[J");    
    }
}
