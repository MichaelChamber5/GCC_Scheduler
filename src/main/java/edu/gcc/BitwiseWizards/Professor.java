package edu.gcc.BitwiseWizards;

/**
 * Professor:
 * Author: Team Bitwise Wizards -Aiden, Micheal, Hannah
 * The Professor class is responsible for creating and managing professors.
 */

public class Professor {

    private int id;
    private String name;
    private double rating;
    private double difficulty;

    public Professor(int id, String name, double rating, double difficulty) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    // this allows future usage
    public double getRating() {
        return rating;
    }
    //future usage
    public void setRating(double rating) {
        this.rating = rating;
    }
    //future usage
    public double getDifficulty() {
        return difficulty;
    }
    //future usage
    public void setDifficulty(double rating) {
        this.rating = difficulty;
    }

    /**
     * Returns the name of the professor.
     * @return
     */

    @Override
    public String toString() {
        return name;
    }
}