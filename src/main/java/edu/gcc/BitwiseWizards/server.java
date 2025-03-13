package edu.gcc.BitwiseWizards;
import static spark.Spark.*;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import freemarker.template.Configuration;
import freemarker.template.Version;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class server {
    public static void main(String[] args) {
        port(4567);

        Configuration freeMarkerConfiguration = new Configuration(new Version(2,3,31));
        freeMarkerConfiguration.setClassForTemplateLoading(server.class, "/templates");
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);

        get("/register", (req, res) ->{
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "register.ftl");
        }, freeMarkerEngine);

        get("/login", (req, res) ->{
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.ftl");
        }, freeMarkerEngine);

        get("/calendar", (req, res) ->{
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "calendar.ftl");
        }, freeMarkerEngine);
    }
}
