package com.example.javafxjdbc;

//obiekty tej klasy przechowuja dane na temat danego pokemona
public class Pokemon {
    private int idPokemon;
    private String name;
    private int hp;
    private int attack;
    private int defense;
    private int speed;
    private String type;

    public Pokemon(int idPokemon, String name, int hp, int attack, int defense, int speed, String type) {
        this.idPokemon = idPokemon;
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.type = type;
    }

    public int getIdPokemon() {
        return idPokemon;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public String getType() {
        return type;
    }
}
