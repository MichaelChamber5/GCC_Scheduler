package edu.gcc.BitwiseWizards;

import java.sql.SQLException;
import java.util.*;
class Main {

    private static User curr_user = null;
    private static Search search;
    private static DatabaseManager dm = null;

    // create new user
    public static void createUser(String email, String password) {
        User user = new User(email, password);
        dm.insertUser(email, password);
        curr_user = user;
    }

    public static void login(String email, String password) {
        try {
//            curr_user = dm.getUser(email, password);
            curr_user = new User(email, password);
        }
//        catch (SQLException e) {
        catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
        }
    }

    public static void logout() {
        curr_user = null;
    }

    public void addScheduleItem(ScheduleItem item) {
        // Implementation here
    }

    public void removeScheduleItem(ScheduleItem item) {
        // Implementation here
    }

    public static void main(String[] args) {
        // launch();
        dm = new DatabaseManager();
        System.out.println(curr_user);
        String email = "proctorhm22@gcc.edu";
        String password = "password";
        createUser(email, password);
        System.out.println(curr_user);
        logout();
        System.out.println(curr_user);
        login(email, password);
        System.out.println(curr_user);
        dm.close();
    }

}