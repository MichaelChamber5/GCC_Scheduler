//package BitwiseWizards;
//
//import edu.gcc.BitwiseWizards.CourseItem;
//import edu.gcc.BitwiseWizards.DatabaseManager;
//import edu.gcc.BitwiseWizards.Search;
//import edu.gcc.BitwiseWizards.User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.when;
//
//public class SearchTest {
//
//    private Search search;
//    private DatabaseManager mockDbm;
//    private User mockUser;
//
//    @BeforeEach
//    void setUp() {
//        // Mock DatabaseManager
//        mockDbm = Mockito.mock(DatabaseManager.class);
//
//        // Initialize Search with mock database
//        search = new Search(mockDbm);
//
//        // Mock User (not used extensively in tests but required for search method)
//        mockUser = Mockito.mock(User.class);
//
//        // Mock Course Data
//        CourseItem course1 = new CourseItem(101, 3, false, "STEM 101", "Intro to Programming",
//                101, 'A', "2025_Spring", "COMP", "Basic programming course",
//                new ArrayList<>(), Map.of('M', List.of(900, 1030)), true);
//
//        CourseItem course2 = new CourseItem(102, 4, false, "STEM 201", "Data Structures",
//                202, 'B', "2025_Spring", "COMP", "Intermediate programming course",
//                new ArrayList<>(), Map.of('T', List.of(1100, 1230)), true);
//
//        CourseItem course3 = new CourseItem(103, 3, false, "STEM 301", "Algorithms",
//                301, 'A', "2025_Spring", "COMP", "Advanced programming course",
//                new ArrayList<>(), Map.of('W', List.of(1300, 1430)), true);
//
//        // Mock search behavior
//        when(mockDbm.searchCoursesByKeyword("Programming"))
//                .thenReturn(new ArrayList<>(List.of(101, 102)));
//
//        when(mockDbm.getCourseByID(101)).thenReturn(course1);
//        when(mockDbm.getCourseByID(102)).thenReturn(course2);
//        when(mockDbm.getCourseByID(103)).thenReturn(course3);
//
//        when(mockDbm.searchCoursesFuzzy("Programming"))
//                .thenReturn(new ArrayList<>(List.of(103)));
//    }
//
//    @Test
//    void testSearchExactMatch() {
//        List<CourseItem> results = search.search("Programming", mockUser, mockDbm);
//
//        assertNotNull(results, "Search results should not be null.");
//        assertEquals(3, results.size(), "Search should return 3 courses (2 exact + 1 fuzzy).");
//
//        assertEquals("Intro to Programming", results.get(0).getName());
//        assertEquals("Data Structures", results.get(1).getName());
//        assertEquals("Algorithms", results.get(2).getName());
//    }
//
//    @Test
//    void testFilterByDepartment() {
//        search.search("Programming", mockUser, mockDbm); // Ensure searchedCourses is populated
//        List<CourseItem> filteredResults = search.filter("COMP", new ArrayList<>(), null, null);
//
//        assertNotNull(filteredResults, "Filtered results should not be null.");
//        assertEquals(3, filteredResults.size(), "All courses belong to COMP.");
//    }
//
//    @Test
//    void testFilterByDays() {
//        search.search("Programming", mockUser, mockDbm); // Populate searchedCourses
//        List<CourseItem> filteredResults = search.filter(null, List.of('M'), null, null);
//
//        assertNotNull(filteredResults, "Filtered results should not be null.");
//        assertEquals(1, filteredResults.size(), "Only one course meets on Monday.");
//        assertEquals("Intro to Programming", filteredResults.get(0).getName());
//    }
//
//    @Test
//    void testFilterByStartTime() {
//        search.search("Programming", mockUser, mockDbm);
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, 10);
//        cal.set(Calendar.MINUTE, 0);
//        Date startTime = cal.getTime();
//
//        List<CourseItem> filteredResults = search.filter(null, new ArrayList<>(), startTime, null);
//
//        assertNotNull(filteredResults, "Filtered results should not be null.");
//        assertEquals(2, filteredResults.size(), "Two courses start after 10:00 AM.");
//        assertEquals("Data Structures", filteredResults.get(0).getName());
//        assertEquals("Algorithms", filteredResults.get(1).getName());
//    }
//
//    @Test
//    void testFilterByEndTime() {
//        search.search("Programming", mockUser, mockDbm);
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, 12);
//        cal.set(Calendar.MINUTE, 0);
//        Date endTime = cal.getTime();
//
//        List<CourseItem> filteredResults = search.filter(null, new ArrayList<>(), null, endTime);
//
//        assertNotNull(filteredResults, "Filtered results should not be null.");
//        assertEquals(1, filteredResults.size(), "Only one course ends before 12:00 PM.");
//        assertEquals("Intro to Programming", filteredResults.get(0).getName());
//    }
//
//}
