package edu.gcc.BitwiseWizards;

import java.util.*;

public class Search {
    private List<CourseItem> searchedCourses;
    private List<CourseItem> filteredCourses;
    private String keywordStr = "";
    private String deptCode = "";
    private List<Character> days = new ArrayList<>();
    private Date start = null;
    private Date end = null;
    private DatabaseManager dbm;

    //mike wuz here
    public Search(DatabaseManager dbm) {
        this.dbm = dbm;
        searchedCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
    }

    public ArrayList<CourseItem> search(String keywordStr, User currUser, DatabaseManager dm) {
        DatabaseManager dbm = new DatabaseManager(dm);
        ArrayList<Integer> courseIDs = dbm.searchCoursesByKeyword(keywordStr);
        ArrayList<CourseItem> courses = new ArrayList<>();

        for (int courseID : courseIDs) {
            courses.add(dbm.getCourseByID(courseID));
        }

        if (courses.isEmpty() || courses.size() < 3) {
            courses.addAll(performFuzzySearch(keywordStr, currUser, dbm));
        }

        this.searchedCourses = courses; // Store the searched courses for filtering
        return courses;
    }

    private List<CourseItem> performFuzzySearch(String keywordStr, User currUser, DatabaseManager dbm) {
        ArrayList<Integer> allCourseIDs = dbm.searchCoursesFuzzy(keywordStr);
        List<CourseItem> bestMatches = new ArrayList<>();

        for (int courseID : allCourseIDs) {
            bestMatches.add(dbm.getCourseByID(courseID));
        }

        return bestMatches;
    }

    public List<CourseItem> filter(String deptCode, List<Character> days, Date start, Date end) {
        this.deptCode = deptCode;
        this.days = days;
        this.start = start;
        this.end = end;

        if (searchedCourses.isEmpty()) {
            return new ArrayList<>();
        }

        // Start filtering using only searchedCourses data
        filteredCourses = new ArrayList<>(searchedCourses);

        // Filter by department code
        if (deptCode != null && !deptCode.isEmpty()) {
            filteredCourses.removeIf(course -> !course.getDepCode().equalsIgnoreCase(deptCode));
        }

        // Filter by days
        if (days != null && !days.isEmpty()) {
            filteredCourses.removeIf(course -> !containsAnyDay(course.getDays(), days));
        }

        // Filter by start time
        if (start != null) {
            filteredCourses.removeIf(course -> {
                Integer courseStart = course.getStartTime();
                return courseStart != null && courseStart < convertDateToTime(start);
            });
        }

        // Filter by end time
        if (end != null) {
            filteredCourses.removeIf(course -> {
                Integer courseEnd = course.getEndTime();
                return courseEnd != null && courseEnd > convertDateToTime(end);
            });
        }

        return filteredCourses;
    }

    private boolean containsAnyDay(List<Character> courseDays, List<Character> requestedDays) {
        for (Character day : requestedDays) {
            if (courseDays.contains(day)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a `Date` object to an integer representing military time.
     * Example: 9:30 AM -> 930, 3:15 PM -> 1515
     */
    private int convertDateToTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return hour * 100 + minute;
    }

}

