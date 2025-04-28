package edu.gcc.BitwiseWizards;

import java.util.*;

public class SearchTester {
    private NewDatabaseManager dbm;
    private Search search;
    private User testUser;

    public SearchTester() throws Exception {
        dbm = new NewDatabaseManager();
        search = new Search(dbm);
        // Create test user with ID 1, username "testUser", and password "test123"
        testUser = new User(1, "testUser", "test123");
    }

    public void runTests() {
        System.out.println("=== Starting Search Tests ===\n");

        // Test 1: Basic Search
//        testBasicSearch1();
//        testBasicSearch2();
//        testBasicSearch3();
//        testBasicSearch4();
//        testBasicSearch5();
//        testBasicSearch6();
//
//        testMultiSearch1();
//        testMultiSearch2();
//
//        testMispelledSearch1();
//
//        testMultiSearch3();

        testEmptySearch();

        mispelledFall();
        mispelledSpring();

        eleeAll();
        eleeFall();
        eleeSpring();

        // Test 2: Fuzzy Search
        //testFuzzySearch();

        // Test 3: Filter Tests
        //testFilters();

        System.out.println("\n=== All Tests Completed ===");
    }

    private void testBasicSearch1() {
        System.out.println("Test 1: Basic Search");
        try {
            System.out.println("SEARCHING FOR: Computer");
            ArrayList<CourseItem> results = search.search("Computer", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testBasicSearch2() {
        System.out.println("Test 2: Basic Search");
        try {
            System.out.println("SEARCHING FOR: accounting");
            ArrayList<CourseItem> results = search.search("accounting", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testBasicSearch3() {
        System.out.println("Test 3: Basic Search");
        try {
            System.out.println("SEARCHING FOR: F");
            ArrayList<CourseItem> results = search.search("F", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testBasicSearch4() {
        System.out.println("Test 4: Basic Search");
        try {
            System.out.println("SEARCHING FOR: Hutchins");
            ArrayList<CourseItem> results = search.search("Hutchins", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testBasicSearch5() {
        System.out.println("Test 5: Basic Search");
        try {
            System.out.println("SEARCHING FOR: STEM");
            ArrayList<CourseItem> results = search.search("STEM", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testBasicSearch6() {
        System.out.println("Test 6: Basic Search");
        try {
            System.out.println("SEARCHING FOR: 141");
            ArrayList<CourseItem> results = search.search("141", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testMultiSearch1() {
        System.out.println("Test 7: Multi Search");
        try {
            System.out.println("SEARCHING FOR: accounting b");
            ArrayList<CourseItem> results = search.search("accounting b", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testMultiSearch2() {
        System.out.println("Test 8: Multi Search");
        try {
            System.out.println("SEARCHING FOR: comp 141 a");
            ArrayList<CourseItem> results = search.search("comp 141 a", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testMultiSearch3() {
        System.out.println("Test 10: Multi Search");
        try {
            System.out.println("SEARCHING FOR: educ 488");
            ArrayList<CourseItem> results = search.search("educ 488", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testMispelledSearch1() {
        System.out.println("Test 9: Mispelled Search");
        try {
            System.out.println("SEARCHING FOR: cmputer");
            ArrayList<CourseItem> results = search.search("cmputer", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testEmptySearch() {
        System.out.println("Test 11: Empty Search");
        try {
            System.out.println("SEARCHING FOR: \"\"");
            ArrayList<CourseItem> results = search.search("", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void mispelledFall() {
        System.out.println("Test: mispelled search fall");
        try {
            System.out.println("SEARCHING FOR: computr");
            ArrayList<CourseItem> results = search.search("computr", "2023_Fall");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void mispelledSpring() {
        System.out.println("Test: mispelled search spring");
        try {
            System.out.println("SEARCHING FOR: computr");
            ArrayList<CourseItem> results = search.search("computr", "2024_Spring");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void eleeFall() {
        System.out.println("Test: elee fall");
        try {
            System.out.println("SEARCHING FOR: elee");
            ArrayList<CourseItem> results = search.search("elee", "2023_Fall");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void eleeSpring() {
        System.out.println("Test: elee spring");
        try {
            System.out.println("SEARCHING FOR: elee");
            ArrayList<CourseItem> results = search.search("elee", "2024_Spring");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void eleeAll() {
        System.out.println("Test: elee all");
        try {
            System.out.println("SEARCHING FOR: elee");
            ArrayList<CourseItem> results = search.search("elee", "");
            System.out.println("Basic search results count: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

//    private void testFuzzySearch() {
//        System.out.println("Test 2: Fuzzy Search");
//        try {
//            // Using a misspelled or partial word to trigger fuzzy search
//            ArrayList<CourseItem> results = search.search("Comput", dbm);
//            System.out.println("Fuzzy search results count: " + results.size());
//            printResults(results);
//        } catch (Exception e) {
//            System.out.println("Error in fuzzy search: " + e.getMessage());
//        }
//        System.out.println();
//    }

    private void testFilters() {
        System.out.println("Filter Tests");
        try {
            // First perform a search to populate searchedCourses
            search.search("", "");

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
                System.out.println(course);
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
