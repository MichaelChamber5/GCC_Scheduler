package edu.gcc.BitwiseWizards;
import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import freemarker.template.Configuration;
import freemarker.template.Version;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class server {
    private static DatabaseManager dbm;


    public static void main(String[] args) {
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

            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                res.redirect("/login?error=Missing+credentials");
                return null;
            }


            int userId = dbm.getUserID(username, password);

            if (userId != -1) {
                User user = new User(userId, username, password);
                List<ScheduleItem> items = dbm.getUserSchedule(userId);
                user.setSchedule(items);
                req.session().attribute("userId", userId);
                res.redirect("/calendar");
            } else {

                res.redirect("/login?error=Invalid+username+or+password");
            }
            return null;
        });

        get("/calendar", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            User user = req.session().attribute("user");
            if (user != null) {

                model.put("scheduleItems", user.getSchedule().getScheduleItems());
            }
            return new ModelAndView(model, "calendar.ftl");
        }, freeMarkerEngine);
    }
}
