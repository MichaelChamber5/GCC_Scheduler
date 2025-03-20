package edu.gcc.BitwiseWizards;

import java.util.*;

/**
 * FilterTester:
 * Author: Team Bitwise Wizards -Aiden
 * The FilterTester class is responsible for testing the Search class.
 * The FilterTester class tests the filter functionality of the Search class.
 * The FilterTester class tests the accuracy of the filter functionality of the Search class.
 */

public class FilterTester {
    private DatabaseManager dbm;
    private Search search;
    private User testUser;

    public FilterTester() {
        dbm = new DatabaseManager();
        search = new Search(dbm);
        testUser = new User(1, "testUser", "test123");
    }

    public void runTests() {
        System.out.println("\n=== Starting Filter Tests ===");

        testFilterByDepartment();
        testFilterByDays();
        testFilterByTimeRange();

        System.out.println("\n=== All Filter Tests Completed ===");
    }

    private void testFilterByDepartment() {
        System.out.println("\nSearching:  Test 1: Filter by Department (COMP)");

        search.search("", "2025_Spring", testUser, dbm); // Get all courses in the semester
        List<CourseItem> beforeFilter = search.searchedCourses;
        List<CourseItem> results = search.filter("COMP", null, null, null);

        System.out.println("Before Filtering (Total Courses): " + beforeFilter.size());
        System.out.println("After Filtering (COMP Only): " + results.size());

        for (CourseItem course : results) {
            System.out.println("\n Passed: " + course.getCourseName() + " - " + course.getDepCode());
        }
    }

    private void testFilterByDays() {
        System.out.println("\n Searching: Test 2: Filter by Days (MWF)");

        search.search("", "2025_Spring", testUser, dbm);
        List<Character> days = Arrays.asList('M', 'W', 'F');
        List<CourseItem> beforeFilter = search.searchedCourses;
        List<CourseItem> results = search.filter(null, days, null, null);

        System.out.println("Before Filtering (Total Courses): " + beforeFilter.size());
        System.out.println("After Filtering (MWF Only): " + results.size());

        for (CourseItem course : results) {
            System.out.println(" \n Passed " + course.getCourseName() + " - Days: " + course.getDays());
        }
    }

    private void testFilterByTimeRange() {
        System.out.println("\nSearching: Test 3: Filter by Time Range (8 AM - 2 PM)");

        search.search("", "2025_Spring", testUser, dbm);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 8);
        Date startTime = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 14);
        Date endTime = cal.getTime();

        List<CourseItem> beforeFilter = search.searchedCourses;
        List<CourseItem> results = search.filter(null, null, startTime, endTime);

        System.out.println("Before Filtering (Total Courses): " + beforeFilter.size());
        System.out.println("After Filtering (8 AM - 2 PM): " + results.size());

        for (CourseItem course : results) {
            System.out.println("\nPassed:" + course.getCourseName() + " - Start Time: " + course.getStartTime());
        }
    }

    public static void main(String[] args) {
        new FilterTester().runTests();
    }
}
