package edu.gcc.BitwiseWizards;

    import java.util.*;

/**
 * SearchTester:
 * Author: Team Bitwise Wizards -Aiden
 * The SearchTester class is responsible for testing the Search class.
 * The SearchTester class tests the search functionality of the Search class.
 * The SearchTester class tests the filter functionality of the Search class.
 * The SearchTester class tests the fuzzy search functionality of the Search class.
 * The SearchTester class tests the accuracy of the fuzzy search functionality of the Search class.
 */

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
           testFuzzySearchAccuracy();
            System.out.println("\n=== All Tests Completed ===");
        }

        // ===================== SEARCH TESTS =====================



        private void printResults(List<CourseItem> courses) {
            System.out.println("Result Count: " + courses.size());
        }

        private void testFuzzySearchAccuracy() {
            System.out.println("\n=== Fuzzy Search Test: Handling Misspellings & Variations ===");

            Map<String, String> testCases = Map.of(
                    "Compter", "Computer",
                    "Computr", "Computer",
                    "Cpmuter", "Computer",
                    "Compter Scince", "Computer",
                    "Sofware Engneering", "Software",
                    "Dta Stuctures", "Data",
                    "Algorthms", "Algorithms",
                    "Netwrk Security", "Network"
            );

            int passed = 0, failed = 0;

            for (var entry : testCases.entrySet()) {
                String keyword = entry.getKey();
                String expectedKeyword = entry.getValue();

                System.out.println("\n Searching for: " + keyword);
                ArrayList<CourseItem> results = search.search(keyword, "2025_Spring", testUser, dbm);

                boolean found = results.stream()
                        .anyMatch(course -> course.getCourseName().toLowerCase().contains(expectedKeyword.toLowerCase()));



                if (found) {
                    System.out.println(" PASSED: Found '" + expectedKeyword + "' in fuzzy results for '" + keyword + "'.");
                    passed++;
                } else {
                    System.out.println(" FAILED: Expected '" + expectedKeyword + "' but no match found.");
                    failed++;
                }
            }

            System.out.println("\n=== Fuzzy Search Results: PASSED " + passed + " / " + (passed + failed) + " ===");
        }

    /**
     * Main method to run the SearchTester class.
     * @param args
     */
    public static void main(String[] args) {
            SearchTester tester = new SearchTester();
            tester.runTests();
        }
    }