package edu.gcc.BitwiseWizards;

import java.util.*;

public class SearchTester {
    private DatabaseManager dbm;
    private Search search;
    private User testUser;

    public SearchTester() {
        dbm = new DatabaseManager();
        search = new Search(dbm);
        // Create test user with ID 1, username "testUser", and password "test123"
        testUser = new User(1, "testUser", "test123");
    }

    public void runTests() {
        System.out.println("=== Starting Search Tests ===\n");

        // Test 1: Basic Search
        testBasicSearch();

        // Test 2: Fuzzy Search
        testFuzzySearch();

        // Test 3: Filter Tests
        testFilters();

        System.out.println("\n=== All Tests Completed ===");
    }

    private void testBasicSearch() {
        System.out.println("Test 1: Basic Search");
        try {
            ArrayList<CourseItem> results = search.search("Computer", testUser, dbm);
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFuzzySearch() {
        System.out.println("Test 2: Fuzzy Search");
        try {
            // Using a misspelled or partial word to trigger fuzzy search
            ArrayList<CourseItem> results = search.search("Comput", testUser, dbm);
            System.out.println("Fuzzy search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in fuzzy search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFilters() {
        System.out.println("Test 3: Filter Tests");
        try {
            // First perform a search to populate searchedCourses
            search.search("", testUser, dbm);

            // Test department filter
            System.out.println("\nTesting COMP department filter:");
            List<CourseItem> deptResults = search.filter("COMP", null, null, null);
            System.out.println("Department filter results count: " + deptResults.size());
            printResults(deptResults);

            // Test days filter
            System.out.println("\nTesting MWF days filter:");
            List<Character> mwfDays = Arrays.asList('M', 'W', 'F');
            List<CourseItem> daysResults = search.filter(null, mwfDays, null, null);
            System.out.println("Days filter results count: " + daysResults.size());
            printResults(daysResults);

            // Test time filter
            System.out.println("\nTesting time filter (8 AM - 2 PM):");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 8);
            Date startTime = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 14);
            Date endTime = cal.getTime();

            List<CourseItem> timeResults = search.filter(null, null, startTime, endTime);
            System.out.println("Time filter results count: " + timeResults.size());
            printResults(timeResults);

        } catch (Exception e) {
            System.out.println("Error in filter tests: " + e.getMessage());
        }
    }

    private void printResults(List<CourseItem> courses) {
        if (courses == null || courses.isEmpty()) {
            System.out.println("No courses found");
            return;
        }

        for (CourseItem course : courses) {
            try {
                System.out.println(String.format("Course: %s, Days: %s, Time: %d - %d",
                        course.getDepCode(),
                        course.getDays() != null ? course.getDays().toString() : "N/A",
                        course.getStartTime(),
                        course.getEndTime()));
            } catch (Exception e) {
                System.out.println("Error printing course: " + e.getMessage());
            }
        }
    }



    public static void main(String[] args) {
        try {
            SearchTester tester = new SearchTester();
            tester.runTests();
        } catch (Exception e) {
            System.out.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
