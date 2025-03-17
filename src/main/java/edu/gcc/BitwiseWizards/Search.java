package edu.gcc.BitwiseWizards;


import org.apache.commons.text.similarity.LevenshteinDistance;
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

    public Search(DatabaseManager dbm) {
        this.dbm = dbm;
        searchedCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
    }

    public ArrayList<CourseItem> search(String keywordStr, User currUser) {
        DatabaseManager dbm = new DatabaseManager();
        ArrayList<Integer> courseIDs = dbm.searchCoursesByKeyword(keywordStr);
        ArrayList<CourseItem> courses = new ArrayList<>();

        // Get exact matches
        for (int courseID : courseIDs) {
            courses.add(dbm.getCourseByID(courseID));

        }

        // If no exact matches or not enough results, perform fuzzy search
        if (courses.isEmpty() || courses.size() < 3) {
            courses.addAll(performFuzzySearch(keywordStr, currUser, dbm));
        }

        return courses;
    }

    private List<CourseItem> performFuzzySearch(String keywordStr, User currUser, DatabaseManager dbm) {
        ArrayList<Integer> allCourseIDs = dbm.searchCoursesFuzzy(keywordStr); // Use fuzzy search
        List<CourseItem> bestMatches = new ArrayList<>();

        for (int courseID : allCourseIDs) {
            bestMatches.add(dbm.getCourseByID(courseID));

        }

        return bestMatches;
    }

//    public List<CourseItem> filter(String deptCode, List<Character> days, Date start, Date end) {
//        DatabaseManager dbm = new DatabaseManager();
//        List<CourseItem> allCourses = new ArrayList<>();
//
//        // Step 1: Get courses from the database based on department
//        ArrayList<Integer> courseIDs = dbm.searchCoursesByDepartment(deptCode);
//        for (int courseID : courseIDs) {
//            CourseItem course = dbm.getCourseById(courseID, -1); // Use -1 for no specific user
//            if (course != null) {
//                allCourses.add(course);
//            }
//        }
//
//        // Step 2: Filter by meeting days
//        if (!days.isEmpty()) {
//            allCourses.removeIf(course -> !course.getDays().containsAll(days));
//        }
//
//        // Step 3: Filter by start and end time
//        if (start != null || end != null) {
//            allCourses.removeIf(course -> {
//                Date courseStart = course.getStartTime();
//                Date courseEnd = course.getEndTime();
//                return (start != null && courseStart.before(start)) ||
//                        (end != null && courseEnd.after(end));
//            });
//        }
//
//        return allCourses;
//    }
}