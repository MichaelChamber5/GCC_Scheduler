//package edu.gcc.BitwiseWizards;
//
//import java.util.*;
//
//class Main {
//
//    private static User curr_user = null;
//    private static Search search;
//    private static DatabaseManager dm;
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
//            login(email, password);
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
//            User user = new User(user_id, email, password);
//            user.setSchedule(dm.getUserSchedule(user_id));
//            curr_user = user;
//        }
//        catch (Exception e) {
//            System.err.println("Login failed: " + e.getMessage());
//            curr_user = null; // should be redundant, but just in case
//        }
//    }
//
//    /**
//     * Sets curr_user to null (effectively redirecting to the login page).
//     */
//    public static void logout() {
//        curr_user = null;
//    }
//
//    public static void addScheduleItem(ScheduleItem item) {
//        boolean added = curr_user.getSchedule().addScheduleItem(item);
//
//        if(added)
//        {
//            if (item instanceof CourseItem) {
//                dm.insertUserCourse(curr_user.getId(), item.getId());
//            }
//            else {
//                // TODO: fix pitem id issues
//                dm.insertPersonalItem(curr_user.getId(), item.getName(), item.getMeetingTimes());
//            }
//        }
//        else
//        {
//            System.out.println("ERROR: item not added to DB");
//        }
//    }
//
//    public static void removeScheduleItem(ScheduleItem item) {
//        if (item instanceof CourseItem) {
//            dm.deleteUserCourse(curr_user.getId(), item.getId());
//        }
//        else {
//            dm.deleteUserPersonalItem(curr_user.getId(), dm.getPersonalItemID(curr_user.getId(), item.getName()));
//        }
//        curr_user.setSchedule(dm.getUserSchedule(curr_user.getId()));
//    }
//
//    // testing...
//    // TODO: actually implement main
//    public static void main(String[] args) {
//
//        // launch();
//
//        // create / initialize database
//        dm = new DatabaseManager();
//        System.out.println("\nInitially curr_user is " + curr_user + ".\n");
//
//        String email = "example@gcc.edu";
//        String password = "password";
//
//        // create user account
//        createUser(email, password);
//        System.out.println("Created user: " + curr_user);
//
//        // create / initialize search
//        String keyword = "Accounting";
//        Search mySearch = new Search(dm);
//        // mySearch.search(keyword, curr_user);
//        ArrayList<CourseItem> courses = mySearch.search(keyword, curr_user,dm );
//        System.out.println("RESULTS:");
//        System.out.println(courses);
//
//        addScheduleItem(courses.get(0));
//        System.out.println("\nadd " + courses.get(0) + " to user schedule: " + curr_user.getSchedule());
//
//        addScheduleItem(courses.get(1));
//        System.out.println("\nadd " + courses.get(1) + " to user schedule: " + curr_user.getSchedule());
//
//        removeScheduleItem(courses.get(1));
//        System.out.println("\nremove " + courses.get(1) + " from user schedule: " + curr_user.getSchedule());
//
//        // create new schedule item
//        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
//        meetingTimes.put('W', new ArrayList<>(Arrays.asList(1100, 1145)));
//        ScheduleItem item = new ScheduleItem("Chapel", meetingTimes);
//
//        // add it to user schedule
//        addScheduleItem(item);
//        System.out.println("\nadd " + item + " to user schedule: " + curr_user.getSchedule());
//
//        logout();
//        System.out.println("\nLogged out: " + curr_user);
//
//        login(email, password);
//        System.out.println("\nLogged in: " + curr_user);
//
//        // reload previously saved schedule
//        System.out.println(curr_user.getSchedule());
//
//        // remove pitem from saved schedule
//        removeScheduleItem(item);
//        System.out.println("\nremove " + item + " from user schedule: " + curr_user.getSchedule());
//
//        // remove course from saved schedule
//        removeScheduleItem(courses.get(0));
//        System.out.println("\nremove " + courses.get(0) + " from user schedule: " + curr_user.getSchedule());
//
//        logout();
//        System.out.println("\nLogged out: " + curr_user);
//
//        login(email, password);
//        System.out.println("\nLogged in: " + curr_user);
//
//        // reload previously saved schedule
//        System.out.println(curr_user.getSchedule());
//
//        dm.close();
//
//    }
//
//}