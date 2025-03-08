package edu.gcc.BitwiseWizards;

import java.sql.SQLException;
import java.util.*;
class Main {

    private static User curr_user = null;
    private static Search search;
    private static DatabaseManager dm = null;

    /**
     * Creates a new user with the specified email and password / updates curr_user accordingly.
     * TODO: password hashing.
     * @param email email of new user
     * @param password password of new user
     */
    public static void createUser(String email, String password) {
        try {
            // add new user to database
            int user_id = dm.insertUser(email, password);
            // initialize user object
            User user = new User(user_id, email, password);
            // set curr_user to new user
            curr_user = user;
        } catch (Exception e) {
            System.out.println("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Checks if email / password correspond to an existing user account / updates curr_user
     * accordingly.
     * @param email user email
     * @param password user password
     */
    public static void login(String email, String password) {
        try {
            // TODO: get dm.getUser() working the way it's supposed to...
            // currently, user schedule isn't loaded from database
//            curr_user = dm.getUser(email, password);
            int user_id = dm.getUser(email, password);
            curr_user = new User(user_id, email, password);
        }
        catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            curr_user = null; // should be redundant, but just in case
        }
    }

    /**
     * Sets curr_user to null (and redirects to login page).
     */
    public static void logout() {
        curr_user = null;
    }

//    public void addScheduleItem(ScheduleItem item) {
//    }

//    public void removeScheduleItem(ScheduleItem item) {
//    }

    public void addCourse(CourseItem item) {
        // Implementation here
    }

    public void addPersonalItem(PersonalItem item) {
        // Implementation here
    }

    public void removeCourse(CourseItem item) {
        // Implementation here
    }

    public void removePersonalItem(PersonalItem item) {
        // Implementation here
    }

    // testing...
    // TODO: actually implement main
    public static void main(String[] args) {
        // launch();

        dm = new DatabaseManager();
        System.out.println("\nInitially curr_user is " + curr_user + ".\n");

        String email = "example@gcc.edu";
        String password = "password";
        createUser(email, password);
        System.out.println("Created user: " + curr_user);

        // add course

        // add personal item

        logout();
        System.out.println("\nLogged out: " + curr_user);

        login(email, password);
        System.out.println("\nLogged in: " + curr_user);

        dm.close();

    }

}