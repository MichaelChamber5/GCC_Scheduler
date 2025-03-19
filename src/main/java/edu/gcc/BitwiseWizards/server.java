package edu.gcc.BitwiseWizards;
import static spark.Spark.*;

import com.google.gson.Gson;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import freemarker.template.Configuration;
import freemarker.template.Version;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.sql.*;
import java.util.*;


public class server {
    private static DatabaseManager dbm;


    public static void main(String[] args) {

        staticFileLocation("/public");

        dbm = new DatabaseManager();

        port(4567);

        Configuration freeMarkerConfiguration = new Configuration(new Version(2,3,31));
        freeMarkerConfiguration.setClassForTemplateLoading(server.class, "/templates");
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);

        get("/register", (req, res) ->{
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "register.ftl");
        }, freeMarkerEngine);


        post("/register", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("confirmPassword");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                res.redirect("/register?error=Missing+username+or+password");
                return null;
            }

            // Use the static DatabaseManager instance
            int userId = dbm.insertUser(username, password);
            if (userId != -1) {
                res.redirect("/login");
            } else {
                res.redirect("/register?error=Registration+failed");
            }
            return null;
        });


        get("/login", (req, res) ->{
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.ftl");
        }, freeMarkerEngine);


        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("confirmPassword");

            if (username == null || username.trim().isEmpty()
||            password == null || password.trim().isEmpty()) {
                res.redirect("/login?error=Missing+credentials");
                return null;
            }

            int userId = dbm.getUserID(username, password);

            if (userId != -1) {
                // Build the User object fully
                User user = new User(userId, username, password);
                List<ScheduleItem> items = dbm.getUserSchedule(userId);
                user.setSchedule(items);

                // Store the entire User object in the session
                req.session().attribute("user", user);

                // Redirect to the calendar page
                res.redirect("/calendar");
            } else {
                res.redirect("/login?error=Invalid+username+or+password");
            }
            return null;
        });

//        get("/calendar", (req, res) -> {
//            Map<String, Object> model = new HashMap<>();
//
//            User user = req.session().attribute("user");
//            if (user != null) {
//
//                model.put("scheduleItems", user.getSchedule().getScheduleItems());
//            }
//            return new ModelAndView(model, "calendar.ftl");
//        }, freeMarkerEngine);

        get("/api/schedule", (req, res) -> {
            // Assume user is stored in session
            User user = req.session().attribute("user");
            if (user == null) {
                res.status(401);
                return "Unauthorized";
            }
            List<ScheduleItem> items = dbm.getUserSchedule(user.getId());
            // Use Jackson or similar library to convert the list to JSON
            ObjectMapper mapper = new ObjectMapper();
            res.type("application/json");
            return mapper.writeValueAsString(items);
        });

        get("/api/courses", (req, res) -> {
            List<CourseItem> courses = dbm.getAllCourses();
            for (Object obj : courses) {
                System.out.println("Object class: " + obj.getClass().getName());
            }
            String json = new Gson().toJson(courses);
            System.out.println("JSON output: " + json);
            res.type("application/json");
            return json;
        });

        get("/calendar", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            User user = req.session().attribute("user");
            if (user != null) {
                model.put("user", user);
            }
            return new ModelAndView(model, "calendar.ftl");
        }, freeMarkerEngine);


        get("/sidebar-courses", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("courses", dbm.getAllCourses()); // This will return the actual courses from the DB.
            return new ModelAndView(model, "sidebar-courses.ftl");
        }, freeMarkerEngine);

        get("/calendar-snippet", (req, res) -> {
            // Return partial snippet of user schedule
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            List<ScheduleItem> schedule = dbm.getUserSchedule(user.getId());
            Map<String, Object> model = new HashMap<>();
            model.put("schedule", schedule);
            return new ModelAndView(model, "calendar-snippet.ftl");
        }, freeMarkerEngine);

        post("/add-course", (req, res) -> {
            String courseIdStr = req.queryParams("courseId");
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            int courseId = Integer.parseInt(courseIdStr);
            dbm.insertUserCourse(user.getId(), courseId);
            res.type("application/json");
            return new Gson().toJson(Collections.singletonMap("success", true));
        });



        post("/remove-course", (req, res) -> {
            // 1) Retrieve the scheduleItemId from the request
            String itemIdStr = req.queryParams("scheduleItemId");
            if (itemIdStr == null) {
                halt(400, "Missing scheduleItemId");
            }

            // 2) Get the user from session
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            int itemId = Integer.parseInt(itemIdStr);

            // 3) Remove the course from user's schedule in the DB
            //    (If it's a course item, you might do something like:)
            dbm.deleteUserCourse(user.getId(), itemId);

            // Or if you store personal items the same way, adjust as needed

            // 4) Return something simple (could be JSON or plain text)
            return "Removed";
        });


        get("/search", (req, res) -> {
            String keyword = req.queryParams("q");
            if (keyword == null || keyword.trim().isEmpty()) {
                res.status(400);
                return "Missing search query";
            }
            User currUser = req.session().attribute("user");
            // Create a Search instance and perform the search
            Search searchInstance = new Search(dbm);
            ArrayList<CourseItem> results = searchInstance.search(keyword, currUser, dbm);

            res.type("application/json");
            return new Gson().toJson(results);
        });

        get("/logout", (req, res) -> {
            req.session().invalidate(); // Clear the session
            res.redirect("/login");      // Redirect to the login page
            return null;
        });




    }
}