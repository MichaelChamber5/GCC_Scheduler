package edu.gcc.BitwiseWizards;

import java.util.*;

class Main {

    private static User curr_user = null;
    private static Search search;
    private static NewDatabaseManager dm;

    // TODO: better way to keep track of the current schedule?
    private static Schedule curr_schedule = null;

    /**
     * Creates a new user with the specified email and password / updates curr_user accordingly.
     * TODO: implement password hashing.
     * TODO: login after creating new account?
     * @param email email of new user
     * @param password password of new user
     */
    public static void createUser(String email, String password) {
        try {
            int user_id = dm.insertUser(email, password);
            if (user_id > 0) {
                login(email, password);
            }
            else {
                System.out.println("Failed to create user: dm.insertUser() issue");
            }
        } catch (Exception e) {
            System.out.println("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Check if email / password correspond to an existing user account and update curr_user
     * accordingly.
     * @param email user email
     * @param password user password
     */
    public static void login(String email, String password) {
        try {
            int user_id = dm.getUserID(email, password);
            if (user_id > 0) {
                User user = new User(user_id, email, password);
                user.setSchedules(dm.getAllUserSchedules(user_id));
                curr_user = user;
            }
            else {
                System.err.println("Login failed: email / password invalid");
            }
        }
        catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            curr_user = null;
        }
    }

    /**
     * Sets curr_user to null (effectively redirecting to the login page).
     */
    public static void logout() {
        curr_user = null;
        curr_schedule = null;
    }

    /**
     * Get schedule with the given id /
     * @param sched_id
     */
    public static void setCurrentSchedule(int sched_id) {
        curr_schedule = dm.getSchduleByID(curr_user.getId(), sched_id);
        if (curr_schedule == null) {
            System.err.println("Failed to set current schedule: user does not have schedule with " +
                    "specified id.");
        }
    }

    /**
     *
     * @param item
     */
    public static void addScheduleItem(ScheduleItem item) {
        // boolean added = curr_user.getSchedule().addScheduleItem(item);
        boolean added = curr_schedule.addScheduleItem(item);

        if(added)
        {
            if (item instanceof CourseItem) {
                dm.addCourseToSchedule(curr_schedule.getID(), item.getId());
            }
            else {
                dm.addPersonalItemToSchedule(curr_schedule.getID(), item.getName(), item.getMeetingTimes());
            }
        }
        else
        {
            System.out.println("ERROR: item not added to DB");
        }
    }

    public static void removeScheduleItem(ScheduleItem item) {
        if (item instanceof CourseItem) {
            dm.removeCourseFromSchedule(curr_schedule.getID(), item.getId());
        }
        else {
            dm.removePersonalItemFromSchedule(curr_schedule.getID(),
                    dm.getPersonalItemID(curr_user.getId(), item.getName()));
        }
        curr_user.setSchedules(dm.getAllUserSchedules(curr_user.getId()));
    }

    // testing...
    // TODO: actually implement main
    public static void main(String[] args) throws Exception {
        // Initialize PDF processor
        PDFProcessor.initialize();

        // create / initialize database
        dm = new NewDatabaseManager();
        System.out.println("\nInitially curr_user is " + curr_user + ".\n");

        String email = "example@gcc.edu";
        String password = "password";

        // create user account
        createUser(email, password);
        System.out.println("Created user: " + curr_user);
        System.out.println(curr_user.getSchedules());

        // create / initialize search
        String keyword = "Accounting";
        Search mySearch = new Search(dm);
        // mySearch.search(keyword, curr_user);
        ArrayList<CourseItem> courses = mySearch.search(keyword, "");
        System.out.println("RESULTS:");
        System.out.println(courses);

        addScheduleItem(courses.get(0));
        System.out.println("\nadd " + courses.get(0) + " to user schedule: " + curr_user.getSchedules());

        addScheduleItem(courses.get(1));
        System.out.println("\nadd " + courses.get(1) + " to user schedule: " + curr_user.getSchedules());

        removeScheduleItem(courses.get(1));
        System.out.println("\nremove " + courses.get(1) + " from user schedule: " + curr_user.getSchedules());

        // create new schedule item
        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
        meetingTimes.put('W', new ArrayList<>(Arrays.asList(1100, 1145)));
        ScheduleItem item = new ScheduleItem(-1, "Chapel", meetingTimes);

        // add it to user schedule
        addScheduleItem(item);
        System.out.println("\nadd " + item + " to user schedule: " + curr_user.getSchedules());

        logout();
        System.out.println("\nLogged out: " + curr_user);

        login(email, password);
        System.out.println("\nLogged in: " + curr_user);

        // reload previously saved schedule
        System.out.println(curr_user.getSchedules());

        // remove pitem from saved schedule
        removeScheduleItem(item);
        System.out.println("\nremove " + item + " from user schedule: " + curr_user.getSchedules());

        // remove course from saved schedule
        removeScheduleItem(courses.get(0));
        System.out.println("\nremove " + courses.get(0) + " from user schedule: " + curr_user.getSchedules());

        logout();
        System.out.println("\nLogged out: " + curr_user);

        login(email, password);
        System.out.println("\nLogged in: " + curr_user);

        // reload previously saved schedule
        System.out.println(curr_user.getSchedules());

        dm.close();

    }

}