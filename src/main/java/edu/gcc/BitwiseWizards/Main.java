//package edu.gcc.BitwiseWizards;
//
//import java.sql.SQLException;
//import java.util.*;
//
//class Main {
//
//    private static User curr_user = null;
//    private static Search search;
//    private static NewDatabaseManager dm;
//
//    // TODO: better way to keep track of the current schedule?
//    private static Schedule curr_schedule = null;
//
//    /**
//     * Creates a new user with the specified email and password / updates curr_user accordingly.
//     * TODO: implement password hashing.
//     * TODO: login after creating new account?
//     * @param email email of new user
//     * @param password password of new user
//     */
//    public static void createUser(String email, String password) {
//        try {
//            int user_id = dm.insertUser(email, password);
//            if (user_id > 0) {
//                login(email, password);
//            }
//            else {
//                System.out.println("Failed to create user: dm.insertUser() issue");
//            }
//        } catch (Exception e) {
//            System.out.println("Failed to create user: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Check if email / password correspond to an existing user account and update curr_user
//     * accordingly.
//     * @param email user email
//     * @param password user password
//     */
//    public static void login(String email, String password) {
//        try {
//            int user_id = dm.getUserID(email, password);
//            if (user_id > 0) {
//                User user = new User(user_id, email, password);
//                user.setSchedules(dm.getAllUserSchedules(user_id));
//                curr_user = user;
//            }
//            else {
//                System.err.println("Login failed: email / password invalid");
//            }
//        }
//        catch (Exception e) {
//            System.err.println("Login failed: " + e.getMessage());
//            curr_user = null;
//        }
//    }
//
//    /**
//     * Sets curr_user to null (effectively redirecting to the login page).
//     */
//    public static void logout() {
//        curr_user = null;
//        curr_schedule = null;
//    }
//
//    /**
//     * TODO
//     * @param sched_name
//     */
//    public static void createNewSchedule(String sched_name) {
//        if (curr_user != null) {
//            dm.insertUserSchedule(curr_user.getId(), sched_name);
//            int sched_id = dm.getScheduleID(curr_user.getId(), sched_name);
//            if (sched_id < 0) {
//                System.out.println("Failed to create new schedule: DB issue");
//            } else {
//                curr_user.setSchedules(dm.getAllUserSchedules(curr_user.getId()));
//                setCurrentSchedule(sched_id);
//            }
//        }
//        else {
//            System.err.println("Failed to create new schedule: curr_user is null.");
//        }
//    }
//
//    /**
//     * Set curr_schedule to the schedule with the given schedule id.
//     * @param sched_id
//     */
//    public static void setCurrentSchedule(int sched_id) {
//        if (curr_user != null) {
//            curr_schedule = dm.getSchduleByID(curr_user.getId(), sched_id);
//            if (curr_schedule == null) {
//                System.err.println("Failed to set current schedule: user does not have a schedule with " +
//                        "the specified id.");
//            }
//        }
//        else {
//            System.err.println("Failed to set current schedule: curr_user is null.");
//        }
//    }
//
//    /**
//     *
//     * @param item
//     */
//    public static void addScheduleItem(ScheduleItem item) {
//        if (curr_user != null && curr_schedule != null) {
//            // boolean added = curr_user.getSchedule().addScheduleItem(item);
//            boolean added = curr_schedule.addScheduleItem(item);
//
//            if (added) {
//                if (item instanceof CourseItem) {
//                    dm.addCourseToSchedule(curr_schedule.getID(), item.getId());
//                } else {
//                    dm.addPersonalItemToSchedule(curr_schedule.getID(), item.getName(), item.getMeetingTimes());
//                }
//                curr_user.setSchedules(dm.getAllUserSchedules(curr_user.getId()));
//            } else {
//                System.out.println("ERROR: item not added to DB");
//            }
//        }
//        else {
//            System.err.println("Failed to add schedule item: curr_user or curr_schedule is null");
//        }
//    }
//
//    public static void removeScheduleItem(ScheduleItem item) {
//        if (curr_user != null && curr_schedule != null) {
//            if (item instanceof CourseItem) {
//                dm.removeCourseFromSchedule(curr_schedule.getID(), item.getId());
//            } else {
//                dm.removePersonalItemFromSchedule(curr_schedule.getID(),
//                        dm.getPersonalItemID(curr_user.getId(), item.getName()));
//            }
//            curr_user.setSchedules(dm.getAllUserSchedules(curr_user.getId()));
//        }
//        else {
//            System.err.println("Failed to remove schedule item: curr_user or curr_schedule is null");
//        }
//    }
//
//    // testing...
//    // TODO: actually implement main
//    public static void main(String[] args) throws Exception {
//        // Initialize PDF processor
//        PDFProcessor.initialize();
//
//        // create / initialize database
//        dm = new NewDatabaseManager();
//        try {
//            dm.dropTables();
//            dm.createTables();
//        } catch (SQLException e) {
//            System.err.println("[RESET TABLES FOR TESTING]");
//        }
//
//        System.out.println("\nInitially curr_user is " + curr_user + ".");
//
//        String email = "example@gcc.edu";
//        String password = "password";
//
//        // create user account
//        System.out.println("\nTEST CREATE USER (1)");
//        createUser(email, password);
//        System.out.println("Created user: " + curr_user);
//
//        String sched1_name = "EX1";
//        String sched2_name = "EX2";
//
//        // create new schedules
//        System.out.println("\nTEST CREATE SCHEDULES (1)");
//        createNewSchedule(sched1_name);
//        createNewSchedule(sched2_name);
//        // set current schedule back to sched1
//        setCurrentSchedule(dm.getScheduleID(curr_user.getId(), sched1_name));
//        System.out.println("curr_user schedules: " + curr_user.getSchedules());
//        System.out.println("curr_schedule: " + curr_schedule);
//
//        // create / initialize search
//        System.out.println("\nTEST SEARCH (1)");
//        String keyword = "Accounting";
//        Search mySearch = new Search(dm);
//        // mySearch.search(keyword, curr_user);
//        ArrayList<CourseItem> courses = mySearch.search(keyword, "");
//        System.out.println("RESULTS:");
//        System.out.println(courses);
//
//        // add courses to curr_schedule
//        System.out.println("\nTEST ADD COURSE TO USER SCHEDULE (1)");
//        addScheduleItem(courses.get(0));
//        System.out.println("add " + courses.get(0) + " to user schedule: " + curr_user.getSchedules());
//        addScheduleItem(courses.get(1));
//        System.out.println("add " + courses.get(1) + " to user schedule: " + curr_user.getSchedules());
//        addScheduleItem(courses.get(2));
//        System.out.println("add " + courses.get(2) + " to user schedule: " + curr_user.getSchedules());
//
//        // remove course from curr_schedule
//        System.out.println("\nTEST REMOVING COURSE FROM USER SCHEDULE (1)");
//        removeScheduleItem(courses.get(2));
//        System.out.println("remove " + courses.get(2) + " from user schedule: " + curr_user.getSchedules());
//
//        // add personal item to curr_schedule
//        System.out.println("\nTEST ADD SCHEDULE ITEM TO USER SCHEDULE (1)");
//        // create personal item
//        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
//        meetingTimes.put(Character.valueOf('W'), new ArrayList<>(Arrays.asList(1100, 1145)));
//        ScheduleItem item = new ScheduleItem(-1, "Chapel", meetingTimes);
//        // add it to schedule
//        addScheduleItem(item);
//        System.out.println("add " + item + " to user schedule: " + curr_user.getSchedules());
//
//        // reload saved schedules
//        System.out.println("\nTEST RELOAD SAVED SCHEDULES (1)");
//        logout();
//        System.out.println("logged out: " + curr_user);
//        login(email, password);
//        System.out.println("logged in: " + curr_user);
//
//        // set curr_schedule
//        System.out.println("\nTEST SET CURRENT SCHEDULE (1)");
//        setCurrentSchedule(curr_user.getSchedules().get(0).getID());
//        System.out.println("curr_schedule: " + curr_schedule);
//
//        // remove items from curr_schedule
//        System.out.println("\nTEST REMOVE SCHEDULE ITEMS (2)");
//        // remove personal item
//        removeScheduleItem(item);
//        System.out.println("remove " + item + " from user schedule: " + curr_user.getSchedules());
//        // remove course
//        removeScheduleItem(courses.get(0));
//        System.out.println("remove " + courses.get(0) + " from user schedule: " + curr_user.getSchedules());
//
//        // reload saved schedules
//        System.out.println("\nTEST RELOAD SAVED SCHEDULES (2)");
//        logout();
//        System.out.println("logged out: " + curr_user);
//        login(email, password);
//        System.out.println("logged in: " + curr_user);
//
//        dm.close();
//
//    }
//
//}