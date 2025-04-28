package edu.gcc.BitwiseWizards;

import java.util.ArrayList;

public class FacultyDummyData {

    public static void main(String[] args) {
        //remove columns that were added
        DatabaseManager dbm = new DatabaseManager();

        fillFacultyRatings(dbm,252);
    }

    public static void setFacultyRatingToRandom(DatabaseManager dbm, int facultyID)
    {
        double rating = Math.random() * 5;
        rating = Math.floor(rating * 10) / 10;
        double difficulty = Math.random() * 5;
        difficulty = Math.floor(difficulty * 10) / 10;
        try
        {
            dbm.updateFacultyRating(facultyID, rating, difficulty);
        }
        catch (Exception e)
        {
            System.out.println("Error updating faculty rating: " + e.getMessage());
        }
    }

    public static void fillFacultyRatings(DatabaseManager dbm, int count)
    {
        for(int i = 1; i <= count; i++)
        {
            setFacultyRatingToRandom(dbm, i);
        }
    }
}
