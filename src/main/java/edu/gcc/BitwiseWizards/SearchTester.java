package edu.gcc.BitwiseWizards;

import java.util.*;

public class SearchTester {
    private DatabaseManager dbm;
    private Search search;
    private User testUser;

    public SearchTester() {
        dbm = new DatabaseManager();
        search = new Search(dbm);
        testUser = new User(1, "testUser", "test123");
    }

    public void runTests() {
        System.out.println("=== Starting Search and Filter Tests ===\n");

        // Run search tests
        testBasicSearch();
        testCaseInsensitiveSearch();
        testFuzzySearch();
        testSpecialCharacterSearch();
        testNoMatchSearch();
        testEmptySearch();

        // Run filter tests
        testFilterByDepartment();
        testFilterByMultipleDepartments();
        testFilterByDays();
        //testFilterByMultipleDays();
        testFilterByStartTime();
        testFilterByTimeRange();

        // New tests: Semester-based search and filtering
        testSearchBySemester();
        //testFilterBySemester();
        // testSearchWithMismatchedSemester();

        System.out.println("\n=== All Tests Completed ===");
    }

    // ===================== SEARCH TESTS =====================

    private void testBasicSearch() {
        System.out.println("Test 1: Basic Search (Exact Match)");
        try {
            ArrayList<CourseItem> results = search.search("Computer", "2025_Spring", testUser, dbm);
            System.out.println("Expected 2+ results, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in basic search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testCaseInsensitiveSearch() {
        System.out.println("Test 2: Case-Insensitive Search");
        try {
            ArrayList<CourseItem> results = search.search("cOmPuTeR", "2025_Spring", testUser, dbm);
            System.out.println("Expected 2+ results (same as 'Computer'), Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in case-insensitive search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFuzzySearch() {
        System.out.println("Test 3: Fuzzy Search (Partial Match)");
        try {
            ArrayList<CourseItem> results = search.search("Comput", "2025_Spring", testUser, dbm);
            System.out.println("Expected at least 1 result, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in fuzzy search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testSpecialCharacterSearch() {
        System.out.println("Test 4: Special Character Search (e.g., Symbols or Numbers)");
        try {
            ArrayList<CourseItem> results = search.search("CS-101", "2025_Spring", testUser, dbm);
            System.out.println("Expected at least 1 result if course names contain symbols, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in special character search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testNoMatchSearch() {
        System.out.println("Test 5: No Matching Courses");
        try {
            ArrayList<CourseItem> results = search.search("Quantum Mechanics", "2025_Spring", testUser, dbm);
            System.out.println("Expected 0 results, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in no match search: " + e.getMessage());
        }
        System.out.println();
    }

    private void testEmptySearch() {
        System.out.println("Test 6: Empty Search Query (Returns All Courses for Semester)");
        try {
            ArrayList<CourseItem> results = search.search("", "2025_Spring", testUser, dbm);
            System.out.println("Expected all courses for Spring 2025, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in empty search: " + e.getMessage());
        }
        System.out.println();
    }

    // ===================== FILTER TESTS =====================

    private void testFilterByDepartment() {
        System.out.println("Test 7: Filter by Department (COMP)");
        try {
            search.search("", "2025_Spring", testUser, dbm);
            List<CourseItem> results = search.filter("COMP", null, null, null);
            System.out.println("Expected only COMP courses, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in department filter: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFilterByMultipleDepartments() {
        System.out.println("Test 8: Filter by Multiple Departments (COMP, MATH)");
        try {
            search.search("", "2025_Spring", testUser, dbm);
            List<CourseItem> results = search.filter("COMP,MATH", null, null, null);
            System.out.println("Expected COMP and MATH courses, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in multiple department filter: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFilterByDays() {
        System.out.println("Test 9: Filter by Days (MWF)");
        try {
            search.search("", "2025_Spring", testUser, dbm);
            List<Character> days = Arrays.asList('M', 'W', 'F');
            List<CourseItem> results = search.filter(null, days, null, null);
            System.out.println("Expected courses meeting on MWF, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in day filter: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFilterByStartTime() {
        System.out.println("Test 10: Filter by Start Time (After 10 AM)");
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 10);
            Date startTime = cal.getTime();
            search.search("", "2025_Spring", testUser, dbm);
            List<CourseItem> results = search.filter(null, null, startTime, null);
            System.out.println("Expected courses starting after 10 AM, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in start time filter: " + e.getMessage());
        }
        System.out.println();
    }

    private void testFilterByTimeRange() {
        System.out.println("Test 11: Filter by Time Range (8 AM - 2 PM)");
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 8);
            Date startTime = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 14);
            Date endTime = cal.getTime();
            search.search("", "2025_Spring", testUser, dbm);
            List<CourseItem> results = search.filter(null, null, startTime, endTime);
            System.out.println("Expected courses between 8 AM - 2 PM, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in time range filter: " + e.getMessage());
        }
        System.out.println();
    }

    private void testSearchBySemester() {
        System.out.println("Test 12: Search by Semester (Fall 2024)");
        try {
            ArrayList<CourseItem> results = search.search("Math", "2024_Fall", testUser, dbm);
            System.out.println("Expected Math courses in Fall 2024, Got: " + results.size());
            printResults(results);
        } catch (Exception e) {
            System.out.println("Error in semester-based search: " + e.getMessage());
        }
        System.out.println();
    }

    private void printResults(List<CourseItem> courses) {
        System.out.println("Result Count: " + courses.size());
    }

    public static void main(String[] args) {
        SearchTester tester = new SearchTester();
        tester.runTests();
    }
}