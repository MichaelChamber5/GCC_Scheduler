package edu.gcc.BitwiseWizards;

import java.sql.SQLException;
import java.util.*;
class Main {

    private static User curr_user = null;
    private static Search search;
    private static DatabaseManager dm = null;

    /**
     * Creates a new user with the specified email and password.
     * Database should be updated accordingly.
     * TODO: password hashing
     * @param email
     * @param password
     */
    public static void createUser(String email, String password) {
        try {
            // add new user to users table
            int user_id = dm.insertUser(email, password);
            // initialize user object
            User user = new User(user_id, email, password);
            // set curr_user to new user
            curr_user = user;
        } catch (Exception e) {
            System.out.println("User with that email already exists.");
            curr_user = null; // should be redundant, but just in case
        }
    }

    public static void login(String email, String password) {
        try {
//            curr_user = dm.getUser(email, password);
            int user_id = dm.getUser(email, password);
            curr_user = new User(user_id, email, password);
        }
        catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
        }
    }

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

        // added course

        // added personal item

        logout();
        System.out.println("\nLogged out: " + curr_user);

        login(email, password);
        System.out.println("\nLogged in: " + curr_user);

        dm.close();

    }

}