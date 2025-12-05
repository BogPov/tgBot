package com.festena.Session;

public class Resources {
    private int people;
    private int food;
    private int army;
    private int gold;
    private int reputation;
    private int technology;

    public Resources() {
        this.people = 100;
        this.army = 100;
        this.food = 100;
        this.gold = 1000;
        this.reputation = 50;
        this.technology = 0;
    }

    public int getPeople() {
        return people;
    }

    public int getFood() {
        return food;
    }

    public int getArmy() {
        return army;
    }

    public int getGold() {
        return gold;
    }

    public int getReputation() {
        return reputation;
    }

    public int getTechnology() {
        return technology;
    }

    public void addPeople(int delta) {
        this.people += delta;
    }

    public void addFood(int delta) {
        this.food += delta;
    }

    public void addArmy(int delta) {
        this.army += delta;
    }

    public void addGold(int delta) {
        this.gold += delta;
    }

    public void addReputation(int delta) {
        this.reputation += delta;
    }

    public void addTechnology(int delta) {
        this.technology += delta;
    }
}
