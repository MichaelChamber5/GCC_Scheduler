package edu.gcc.BitwiseWizards;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import freemarker.template.Configuration;
import freemarker.template.Version;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class server {
    private static DatabaseManager dbm;

    public static void main(String[] args) {
        dbm = new DatabaseManager();
        port(4567);

        Configuration freeMarkerConfiguration = new Configuration(new Version(2,3,31));
        freeMarkerConfiguration.setClassForTemplateLoading(server.class, "/templates");
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);

        // ============ Basic Registration/Login ============

        get("/register", (req, res) -> {
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

            int userId = dbm.insertUser(username, password);
            if (userId != -1) {
                res.redirect("/login");
            } else {
                res.redirect("/register?error=Registration+failed");
            }
            return null;
        });

        get("/login", (req, res) -> {
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
                req.session().attribute("user", user);
                res.redirect("/calendar");
            } else {
                res.redirect("/login?error=Invalid+username+or+password");
            }
            return null;
        });

        // ============ Calendar Endpoints ============

        get("/calendar", (req, res) -> {
            return new ModelAndView(null, "calendar.ftl");
        }, freeMarkerEngine);

        get("/calendar-snippet", (req, res) -> {
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            List<ScheduleItem> schedule = dbm.getUserSchedule(user.getId());
            Map<String, Object> model = new HashMap<>();
            model.put("schedule", schedule);
            return new ModelAndView(model, "calendar-snippet.ftl");
        }, freeMarkerEngine);

        // ============ API for schedule + courses ============

        get("/api/schedule", (req, res) -> {
            User user = req.session().attribute("user");
            if (user == null) {
                res.status(401);
                return "Unauthorized";
            }
            List<Integer> ids = dbm.getUserCourseIds(user.getId());
            List<CourseItem> schedule = new ArrayList<>();
            for (Integer courseId : ids) {
                CourseItem c = dbm.getCourseByID(courseId);
                if (c != null) {
                    schedule.add(c);
                }
            }
            res.type("application/json");
            return new Gson().toJson(schedule);
        });

        get("/api/courses", (req, res) -> {
            List<CourseItem> courses = dbm.getAllCourses();
            String json = new Gson().toJson(courses);
            res.type("application/json");
            return json;
        });

        // ============ Add/Remove Course Endpoints ============

        post("/add-course", (req, res) -> {
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            int courseId = Integer.parseInt(req.queryParams("courseId"));
            dbm.insertUserCourse(user.getId(), courseId);
            res.type("application/json");
            return new Gson().toJson(Collections.singletonMap("success", true));
        });

        post("/remove-course", (req, res) -> {
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            int itemId = Integer.parseInt(req.queryParams("scheduleItemId"));
            dbm.deleteUserCourse(user.getId(), itemId);
            return "Removed";
        });

        // ============ SEARCH Endpoint (with advanced filtering) ============
        get("/search", (req, res) -> {
            try {
                User currUser = req.session().attribute("user");
                if (currUser == null) {
                    res.status(401);
                    return "Not logged in";
                }

                // Basic search params
                String keyword = Optional.ofNullable(req.queryParams("q")).orElse("");
                String semester = Optional.ofNullable(req.queryParams("semester")).orElse("");

                // Advanced filter params
                String dept = req.queryParams("dept");        // e.g. "COMP"
                String daysParam = req.queryParams("days");   // e.g. "MWF"
                String startTimeStr = req.queryParams("start"); // e.g. "1000" for 10:00
                String endTimeStr   = req.queryParams("end");   // e.g. "1400" for 2:00 PM

                System.out.println("DEBUG: Search -> keyword='" + keyword + "', semester='" + semester +
                        "', dept='" + dept + "', days='" + daysParam +
                        "', start='" + startTimeStr + "', end='" + endTimeStr + "'");

                // 1) search
                Search searchInstance = new Search(dbm);
                ArrayList<CourseItem> results = searchInstance.search(keyword, semester, currUser, dbm);

                // 2) parse advanced filter input
                List<Character> daysList = new ArrayList<>();
                if (daysParam != null && !daysParam.trim().isEmpty()) {
                    for (char c : daysParam.toUpperCase().toCharArray()) {
                        if ("MTWRF".indexOf(c) >= 0) {
                            daysList.add(c);
                        }
                    }
                }

                // For time, we only store them as "Date" for filter
                // e.g. "1000" -> we parse it into a Date that has time 10:00
                // We'll make a helper parseTime method
                Date startDate = parseTime(startTimeStr); // might be null
                Date endDate   = parseTime(endTimeStr);   // might be null

                // 3) filter if any advanced param is present
                boolean useFilter = (dept != null && !dept.trim().isEmpty()) ||
                        (!daysList.isEmpty()) ||
                        (startDate != null) ||
                        (endDate != null);

                if (useFilter) {
                    List<CourseItem> filtered = searchInstance.filter(dept, daysList, startDate, endDate);
                    results = new ArrayList<>(filtered);
                }

                // Mark onSchedule for each course
                List<Integer> userIds = dbm.getUserCourseIds(currUser.getId());
                Set<Integer> userSet = new HashSet<>(userIds);
                for (CourseItem c : results) {
                    boolean onSch = userSet.contains(c.getId());
                    c.setOnSchedule(onSch);
                }

                Gson gson = new GsonBuilder().create();
                res.type("application/json");
                return gson.toJson(results);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Server error: " + e.getMessage();
            }
        });
    }

    /**
     * Helper to parse a string like "930" or "1330" into a Date object with that hour & minute.
     * Returns null if input is empty or invalid.
     */
    private static Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        // We'll treat the time like "HHmm" (e.g. "0930")
        // Convert it to "HH:mm" format, then parse with SimpleDateFormat
        try {
            String trimmed = timeStr.trim();
            if (trimmed.length() < 3) {
                return null;
            }
            // e.g. "1330" -> "13:30", "930" -> "09:30"
            while (trimmed.length() < 4) {
                trimmed = "0" + trimmed; // pad left if needed
            }
            String hh = trimmed.substring(0,2);
            String mm = trimmed.substring(2);
            String hhmm = hh + ":" + mm;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.parse(hhmm);
        } catch (ParseException e) {
            System.err.println("parseTime error: " + e.getMessage());
            return null;
        }
    }
}
