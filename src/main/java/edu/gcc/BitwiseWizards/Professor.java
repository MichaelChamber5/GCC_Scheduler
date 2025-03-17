package edu.gcc.BitwiseWizards;

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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double rating) {
        this.rating = difficulty;
    }

    @Override
    public String toString() {
        return name;
    }
}