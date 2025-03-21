package edu.gcc.BitwiseWizards;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;

public class Search {
    List<CourseItem> searchedCourses;
    private List<CourseItem> filteredCourses;
    private String keywordStr = "";
    private String semester = "";
    private String deptCode = "";
    private List<Character> days = new ArrayList<>();
    private Date semesterStart = null;
    private Date start = null;
    private Date end = null;
    private DatabaseManager dbm;

    public Search(DatabaseManager dbm) {
        this.dbm = dbm;
        searchedCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
    }

    public ArrayList<CourseItem> search(String keywordStr, String semester, User currUser, DatabaseManager dm) {
        DatabaseManager dbm = new DatabaseManager(dm);
        this.semester = semester;  // Store the selected semester

        ArrayList<Integer> courseIDs = dbm.searchCoursesByKeyword(keywordStr);
        ArrayList<CourseItem> courses = new ArrayList<>();

        // Retrieve courses that match the keyword and semester
        for (int courseID : courseIDs) {
            CourseItem course = dbm.getCourseByID(courseID);
            if (course != null && course.getSemester().equalsIgnoreCase(semester)) {
                courses.add(course);
            }
        }

        // If no exact matches or not enough results, perform fuzzy search
        if (courses.isEmpty() || courses.size() < 3) {
            courses.addAll(performFuzzySearch(keywordStr, semester, currUser, dbm));
        }

        this.searchedCourses = courses; // Store the searched courses for filtering
        return courses;
    }

    private List<CourseItem> performFuzzySearch(String keywordStr, String semester, User currUser, DatabaseManager dbm) {
        ArrayList<Integer> allCourseIDs = dbm.searchAllCourses();
        List<CourseItem> bestMatches = new ArrayList<>();
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        TreeMap<Integer, List<CourseItem>> sortedMatches = new TreeMap<>();

        System.out.println("\n Debug: Available Courses in DB:");

        for (int courseID : allCourseIDs) {
            CourseItem course = dbm.getCourseByID(courseID);
            if (course != null && course.getSemester().equalsIgnoreCase(semester)) {
                // System.out.println(" - " + course.getCourseName()); // PRINT ALL COURSE NAMES

                String courseName = course.getCourseName().toLowerCase();
                String keywordLower = keywordStr.toLowerCase();


                String[] keywordWords = keywordLower.split("\\s+");
                String[] courseWords = courseName.split("\\s+");

                int totalDistance = 0;
                int wordMatches = 0;

                for (String kw : keywordWords) {
                    int bestWordDistance = Integer.MAX_VALUE;  // Keep track of best match for each keyword word

                    for (String cw : courseWords) {
                        int dist = levenshtein.apply(kw, cw);  // Compute Levenshtein distance

                        if (dist < bestWordDistance) {
                            bestWordDistance = dist;  //  Store the closest match
                        }
                    }

                    if (bestWordDistance < 3) {  // Allow up to 2 mistakes per word
                        wordMatches++;
                    }
                    totalDistance += bestWordDistance;
                }

                //  Adjust threshold dynamically:
                // - Require at least half of keyword words to match
                // - Keep total distance relative to course name length
                boolean isMatch = (wordMatches >= keywordWords.length / 2) &&
                        (totalDistance <= courseName.length() / 3);

                if (isMatch) {
                    sortedMatches.putIfAbsent(totalDistance, new ArrayList<>());
                    sortedMatches.get(totalDistance).add(course);

                }
            }
        }

        for (List<CourseItem> courses : sortedMatches.values()) {
            bestMatches.addAll(courses);
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

        // Filter by semester (only courses from the searched semester)
        filteredCourses.removeIf(course -> !course.getSemester().equalsIgnoreCase(semester));

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
    //

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