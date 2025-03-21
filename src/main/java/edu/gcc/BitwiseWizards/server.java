package edu.gcc.BitwiseWizards;

import static spark.Spark.*;
//
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
    // This DatabaseManager handles all database operations.
    private static DatabaseManager dbm;

    public static void main(String[] args) {
        // Initialize the database manager.
        dbm = new DatabaseManager();
        // Set the port to listen on.
        port(4567);

        // Configure FreeMarker template engine.
        Configuration freeMarkerConfiguration = new Configuration(new Version(2, 3, 31));
        freeMarkerConfiguration.setClassForTemplateLoading(server.class, "/templates");
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);

        // Redirect requests to "/" to the login page.
        get("/", (req, res) -> {
            res.redirect("/login");
            return null;
        });

        // Display the registration page.
        get("/register", (req, res) -> {

            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "register.ftl");
        }, freeMarkerEngine);
      
        // Process registration form submissions.

        post("/register", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("confirmPassword");

            // Check if the username or password is missing.

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                res.redirect("/register?error=Missing+username+or+password");
                return null;
            }

            // Insert the new user into the database.
            int userId = dbm.insertUser(username, password);
            if (userId != -1) {
                // Registration succeeded; redirect to the login page.
                res.redirect("/login");
            } else {
                // Registration failed; redirect back with an error message.
                res.redirect("/register?error=Registration+failed");
            }
            return null;
        });

        // Display the login page.
        get("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.ftl");
        }, freeMarkerEngine);

        // Process login form submissions.
        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("confirmPassword");
            // Validate credentials are provided.
            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                res.redirect("/login?error=Missing+credentials");
                return null;
            }
            // Verify user credentials against the database.
            int userId = dbm.getUserID(username, password);
            if (userId != -1) {
                // Create a new user instance and retrieve their schedule.
                User user = new User(userId, username, password);
                List<ScheduleItem> items = dbm.getUserSchedule(userId);
                user.setSchedule(items);
                // Save the user in the session.
                req.session().attribute("user", user);
                // Redirect to the calendar view.
                res.redirect("/calendar");
            } else {
                // If credentials are incorrect, redirect back with an error.

                res.redirect("/login?error=Invalid+username+or+password");
            }
            return null;
        });

        // Render the main calendar page.
        get("/calendar", (req, res) -> {
            return new ModelAndView(null, "calendar.ftl");
        }, freeMarkerEngine);

        // Render a snippet of the calendar for asynchronous updates.
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

        // API endpoint to return the user's schedule in JSON format.
        get("/api/schedule", (req, res) -> {

            User user = req.session().attribute("user");
            if (user == null) {
                res.status(401);
                return "Unauthorized";
            }
            // Retrieve course IDs and then fetch each corresponding course.
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

        // API endpoint to return all courses in JSON.
        get("/api/courses", (req, res) -> {
            List<CourseItem> courses = dbm.getAllCourses();
            String json = new Gson().toJson(courses);
            res.type("application/json");
            return json;
        });

        // Endpoint to add a course to the user's schedule.
        post("/add-course", (req, res) -> {
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            int courseId = Integer.parseInt(req.queryParams("courseId"));

            // Fetch the course to be added.
            CourseItem newCourse = dbm.getCourseByID(courseId);
            if(newCourse == null){
                res.status(404);
                return new Gson().toJson(Collections.singletonMap("error", "Course not found"));
            }

            // Check the current schedule for any conflicting courses.
            List<ScheduleItem> currentSchedule = dbm.getUserSchedule(user.getId());
            for (ScheduleItem scheduledItem : currentSchedule) {
                if (scheduledItem instanceof CourseItem) {
                    CourseItem scheduledCourse = (CourseItem) scheduledItem;
                    if (newCourse.conflicts(scheduledCourse)) {
                        res.status(409);
                        return new Gson().toJson(Collections.singletonMap("error",
                                "Course " + newCourse.getName() + " conflicts with " +
                                        scheduledCourse.getName() + ". Please remove the conflicting course."));
                    }
                }
            }

            // If no conflicts, insert the course into the user's schedule.
            dbm.insertUserCourse(user.getId(), courseId);
            res.type("application/json");
            return new Gson().toJson(Collections.singletonMap("success", true));
        });

        // Endpoint to remove a course from the user's schedule.
        post("/remove-course", (req, res) -> {
            User user = req.session().attribute("user");
            if (user == null) {
                halt(401, "Not logged in");
            }
            int itemId = Integer.parseInt(req.queryParams("scheduleItemId"));
            dbm.deleteUserCourse(user.getId(), itemId);
            return "Removed";
        });

        // Search endpoint with support for advanced filtering.
        get("/search", (req, res) -> {
            try {
                User currUser = req.session().attribute("user");
                if (currUser == null) {
                    res.status(401);
                    return "Not logged in";
                }

                // Retrieve basic search parameters.
                String keyword = Optional.ofNullable(req.queryParams("q")).orElse("");
                String semester = Optional.ofNullable(req.queryParams("semester")).orElse("");

                // Retrieve advanced filter parameters.
                String dept = req.queryParams("dept");
                String daysParam = req.queryParams("days");
                String startTimeStr = req.queryParams("start");
                String endTimeStr   = req.queryParams("end");

                System.out.println("DEBUG: Search -> keyword='" + keyword + "', semester='" + semester +
                        "', dept='" + dept + "', days='" + daysParam +
                        "', start='" + startTimeStr + "', end='" + endTimeStr + "'");

                // Perform an initial search based on the keyword and semester.
                Search searchInstance = new Search(dbm);
                ArrayList<CourseItem> results = searchInstance.search(keyword, semester, currUser, dbm);

                // Convert the days filter string into a list of valid day characters.
                List<Character> daysList = new ArrayList<>();
                if (daysParam != null && !daysParam.trim().isEmpty()) {
                    for (char c : daysParam.toUpperCase().toCharArray()) {
                        if ("MTWRF".indexOf(c) >= 0) {
                            daysList.add(c);
                        }
                    }
                }

                // Parse the start and end time strings into Date objects.
                Date startDate = parseTime(startTimeStr);
                Date endDate   = parseTime(endTimeStr);

                // Determine if advanced filtering should be applied.
                boolean useFilter = (dept != null && !dept.trim().isEmpty()) ||
                        (!daysList.isEmpty()) ||
                        (startDate != null) ||
                        (endDate != null);

                if (useFilter) {
                    List<CourseItem> filtered = searchInstance.filter(dept, daysList, startDate, endDate);
                    results = new ArrayList<>(filtered);
                }

                // Mark courses as being on the user's schedule if applicable.
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
     * Helper method to parse a string like "930" or "1330" into a Date object with that hour and minute.
     * Returns null if the input is empty or invalid.
     */
    private static Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = timeStr.trim();
            if (trimmed.length() < 3) {
                return null;
            }
            // Pad the string with a leading zero if needed
            while (trimmed.length() < 4) {
                trimmed = "0" + trimmed;
            }
            String hh = trimmed.substring(0, 2);
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