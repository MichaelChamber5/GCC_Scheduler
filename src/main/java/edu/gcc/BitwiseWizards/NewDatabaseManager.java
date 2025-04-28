package edu.gcc.BitwiseWizards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * TODO
 */

public class NewDatabaseManager {

    private Connection connection = null;
    private static final String DB_URL = "jdbc:sqlite:GCC_Scheduler.db";
    private static final String JSON_FILE = "/data_wolfe.json";

    // ############################################################################
    // DB CONNECTION / INITIALIZATION
    // ############################################################################

    /**
     * Initialize database connection / table data.
     */
    public NewDatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("\nSuccessfully connected to database.");
            initializeDatabase();
        } catch(SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Close database connection.
     */
    protected void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("\nSuccessfully closed database connection.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to close database connection: " + e.getMessage());
        }
    }

    /**
     * Initialize data by creating / populating tables.
     * TODO: comment out dropTables()
     */
    private void initializeDatabase() {
        try {
            dropTables();
            createTables();
            populateTables();
            System.out.println("Successfully initialized database.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Creates the following tables:
     *      departments         (dept_id, dept_code, dept_name)
     *      faculty             (faculty_id, faculty_name, avg_rating, avg_difficulty)
     *      courses             (course_id, credits, is_lab, is_open, location, ...)
     *      course_faculty      (course_id, faculty_id)
     *      users               (user_id, user_email, user_password)
     *      user_schedules      (user_id, sched_id)
     *      user_courses        (sched_id, course_id)
     *      user_personal_items (sched_id, pitem_id, pitem_name)
     *      time_slots          (time_id, day, start, end)
     *      course_time_slots   (course_id, time_id)
     *      pitem_time_slots    (pitem_id, time_id)
     * @throws SQLException if commands fail
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // DEPARTMENTS (dept_id, "COMP", "Computer Science")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    dept_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    dept_code TEXT NOT NULL UNIQUE,
                    dept_name TEXT
                )
            """);
            // COURSES (course_id, "SOFTWARE ENGINEERING", "COMP", 350, "B", "2024_Spring")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    course_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    credits INTEGER NOT NULL,
                    is_lab BOOLEAN NOT NULL,
                    is_open BOOLEAN NOT NULL,
                    location TEXT NOT NULL,
                    course_name TEXT NOT NULL,
                    course_number INTEGER NOT NULL,
                    open_seats INTEGER NOT NULL,
                    section_id TEXT NOT NULL,
                    semester TEXT NOT NULL,
                    dept_id INTEGER NOT NULL,
                    total_seats INTEGER NOT NULL,
                    FOREIGN KEY (dept_id) REFERENCES departments(dept_id),
                    UNIQUE(dept_id, course_number, section_id, semester)
                )
            """);
            // FACULTY (faculty_id, "Hutchins, Jonathan O.", 4.3, 2.5)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faculty (
                    faculty_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    faculty_name TEXT NOT NULL UNIQUE,
                    avg_rating REAL,
                    avg_difficulty REAL
                )
            """);
            // COURSE_FACULTY (course_id, faculty_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS course_faculty (
                    course_id INTEGER NOT NULL,
                    faculty_id INTEGER NOT NULL,
                    PRIMARY KEY (course_id, faculty_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id),
                    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
                )
            """);
            // TIME_SLOTS (time_id, 'W', 1100, 1145)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS time_slots (
                    time_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    day TEXT NOT NULL,
                    start INTEGER NOT NULL,
                    end INTEGER NOT NULL,
                    UNIQUE(day, start, end)
                )
            """);
            // COURSE_TIME_SLOTS (course_id, time_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS course_time_slots (
                    course_id INTEGER NOT NULL,
                    time_id INTEGER NOT NULL,
                    PRIMARY KEY (course_id, time_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id),
                    FOREIGN KEY (time_id) REFERENCES time_slots(time_id)
                )
            """);
            // USERS (user_id, "proctorhm22@gcc.edu", "password")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_email TEXT NOT NULL UNIQUE,
                    user_password TEXT NOT NULL
                )
            """);
            // USER_SCHEDULES (user_id, sched_id, "Senior Fall")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_schedules (
                    user_id TEXT NOT NULL,
                    sched_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sched_name TEXT NOT NULL UNIQUE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
            """);
            // USER_COURSES (sched_id, course_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_courses (
                    sched_id INTEGER NOT NULL,
                    course_id INTEGER NOT NULL,
                    PRIMARY KEY (sched_id, course_id),
                    FOREIGN KEY (sched_id) REFERENCES user_schedules(sched_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id)
                )
            """);
            // USER_PERSONAL_ITEMS (sched_id, pitem_id, "Chapel")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_personal_items (
                    sched_id INTEGER NOT NULL,
                    pitem_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pitem_name TEXT NOT NULL,
                    UNIQUE(sched_id, pitem_name),
                    FOREIGN KEY (sched_id) REFERENCES user_schedules(sched_id)
                )
            """);
            // PITEM_TIME_SLOTS table (pitem_id, time_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pitem_time_slots (
                    pitem_id INTEGER NOT NULL,
                    time_id INTEGER NOT NULL,
                    PRIMARY KEY (pitem_id, time_id),
                    FOREIGN KEY (pitem_id) REFERENCES personal_items(pitem_id),
                    FOREIGN KEY (time_id) REFERENCES time_slots(time_id)
                )
            """);
        }
    }

    /**
     * Drops all tables associated with user data (i.e., users, personal_items, user_courses,
     * and pitem_time_slots).
     * @throws SQLException if commands fail
     */
    private void dropTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                DROP TABLE IF EXISTS pitem_time_slots;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS user_personal_items;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS user_courses;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS user_schedules;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS users;
            """);
        }
    }

    /**
     * Fills tables with course catalogue data.
     */
    private void populateTables() {
        if (getCourseCount() == 0) {
            loadCoursesFromJson();
        }
    }

    /**
     * Loads courses from JSON file into the database.
     */
    private void loadCoursesFromJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getResourceAsStream(JSON_FILE);
            if (inputStream == null) {
                throw new RuntimeException("Cannot find " + JSON_FILE);
            }
            JsonNode rootNode = mapper.readTree(inputStream);
            JsonNode coursesNode = rootNode.get("classes");
            System.out.println("loading courses from JSON...");
            if (coursesNode != null && coursesNode.isArray()) {
                for (JsonNode courseNode : coursesNode) {
                    processCourseNode(courseNode);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading JSON data: " + e.getMessage());
        }
    }

    /**
     * Processes a given course node.
     * @param courseNode course node from JSON file
     * @throws SQLException if commands fail
     */
    private void processCourseNode(JsonNode courseNode) throws SQLException {
        // extract basic course information
        int credits = getIntValue(courseNode, "credits");
        boolean isLab = Objects.equals(getTextValue(courseNode, "is_lab"), "true");
        boolean isOpen = Objects.equals(getTextValue(courseNode, "is_open"), "true");
        String location = getTextValue(courseNode, "location");
        String courseName = getTextValue(courseNode, "name");
        int courseNumber = getIntValue(courseNode, "number");
        int openSeats = getIntValue(courseNode, "open_seats");
        String section = getTextValue(courseNode, "section");
        String semester = getTextValue(courseNode, "semester");
        String subject = getTextValue(courseNode, "subject");
        int total_seats = getIntValue(courseNode, "total_seats");
        try {
            // insert department / get its ID
            int deptId = getDeptID(subject);
            if (deptId == -1) {
                deptId = insertDepartment(subject, "");
            }
            // ensure section is not null or empty
            if (section == null || section.isEmpty()) {
                throw new SQLException("Section code is missing for course " + courseName);
            }
            // insert course / get its ID
            int courseId = insertCourse(credits, isLab, isOpen, location, courseName, courseNumber,
                    openSeats, section, semester, deptId, total_seats);
            // process faculty
            JsonNode facultyNode = courseNode.get("faculty");
            if (facultyNode != null && facultyNode.isArray()) {
                for (JsonNode faculty : facultyNode) {
                    if (faculty != null && !faculty.isNull()) {
                        int facultyId = getFacultyID(faculty.asText());
                        if (facultyId == -1) {
                            facultyId = insertFaculty(faculty.asText(), 0, 0);
                        }
                        insertCourseFaculty(courseId, facultyId);
                    }
                }
            }
            // process time slots
            JsonNode timesNode = courseNode.get("times");
            if (timesNode != null && timesNode.isArray()) {
                for (JsonNode timeNode : timesNode) {
                    if (timeNode != null && !timeNode.isNull()) {
                        String day = getTextValue(timeNode, "day");
                        String startTime = getTextValue(timeNode, "start_time");
                        String endTime = getTextValue(timeNode, "end_time");
                        if (day != null && startTime != null && endTime != null) {
                            int start = -1;
                            int end = -1;
                            try {
                                // "10:50:00" -> 1050
                                start = Integer.parseInt(startTime.substring(0, 2) + startTime.substring(3, 5));
                                end = Integer.parseInt(endTime.substring(0, 2) + endTime.substring(3, 5));
                            } catch (NumberFormatException e) {
                                System.err.println("Error processing course time slot " +
                                                   "(cannot convert string to integer): " + e.getMessage());
                            }
                            int time_id = getTimeSlotID(day, start, end);
                            if (time_id == -1) {
                                time_id = insertTimeSlot(day, start, end);
                            }
                            insertCourseTimeSlot(time_id, courseId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error processing course: " + courseName);
            System.err.println("Error details: " + e.getMessage());
            throw e;
        }
    }

    /**
     * @return the number of courses in the courses table or -1 if SQLException occurred
     */
    protected int getCourseCount() {
        String sql = "SELECT COUNT(*) FROM courses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get course count: " + e.getMessage());
            return -1;
        }
    }

    // helper methods for safely getting values from JsonNode

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private int getIntValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asInt() : 0;
    }


    // ############################################################################
    // DB ACCESS CALLS
    // ############################################################################

    /*
    DEPARTMENTS
    - private int insertDepartment(String dept_code, String dept_name)
    - protected int getDeptID(String dept_code)
    - protected String getDeptCode(int dept_id)

    COURSES
    - private int insertCourse(...)
    - protected int getCourseID(int dept_id, int course_number, String section, String semester)
    - protected CourseItem getCourseByID(int course_id)
    - protected List<CourseItem> getAllCourses()
    - protected ArrayList<Integer> getAllCourseIds()

    FACULTY
    - private int insertFaculty(String faculty_name, double avg_rating, double avg_difficulty)
    - protected int getFacultyID(String faculty_name)

    COURSE_FACULTY
    - private void insertCourseFaculty(int course_id, int faculty_id)
    - protected ArrayList<Professor> getCourseFaculty(int course_id)

    TIME_SLOTS
    - protected int insertTimeSlot(String day, int start, int end)
    - protected int getTimeSlotID(String day, int start, int end)
    - protected Map<Character, List<Integer>> getMeetingTime(int time_id)

    COURSE_TIME_SLOTS
    - private void insertCourseTimeSlot(int time_id, int course_id)
    - protected Map<Character, List<Integer>> getCourseMeetingTimes(int course_id)

    USERS
    - protected int insertUser(String user_email, String user_password)
    - protected int getUserID(String user_email, String user_password)
    - protected void updateUserEmail(int user_id, String new_email)
    - protected void updateUserPassword(int user_id, String new_password)
    - protected void deleteUser(int user_id)

    USER_SCHEDULES
    - protected int insertUserSchedule(int user_id, String sched_name)
    = protected int getScheduleID(int user_id, String sched_name)
    - protected ArrayList<ScheduleItem> getScheduleItems(int sched_id)
    - protected Schedule getScheduleObject(int user_id, int sched_id)
    - protected ArrayList<Schedule> getAllUserSchedules(int user_id)
    - protected void deleteUserSchedule(int user_id, int sched_id)

    USER_COURSES
    - protected void addCourseToSchedule(int sched_id, int course_id)
    - protected void removeCourseFromSchedule(int sched_id, int course_id)
    - protected ArrayList<CourseItem> getScheduleCourses(int sched_id)
    - protected List<Integer> getScheduleCourseIds(int sched_id)
    - protected boolean courseInSchedule(int sched_id, int courseId)
    - TODO: protected void removeAllCoursesFromSchedule(int sched_id)

    USER_PERSONAL_ITEMS
    - protected int addPersonalItemToSchedule(int sched_id, String pitem_name, Map<Character,
            List<Integer>> meetingTimes)
    - protected int getPersonalItemID(int sched_id, String pitem_name)
    - protected ScheduleItem getPersonalItemByID(int pitem_id)
    - protected void removePersonalItemFromSchedule(int sched_id, int pitem_id)
    - protected ArrayList<ScheduleItem> getSchedulePersonalItems(int sched_id)
    - TODO: protected void removeAllPersonalItemsFromSchedule(int sched_id)

    PITEM_TIME_SLOTS
    - void insertPersonalItemTimeSlot(int time_id, int pitem_id)
    - Map<Character, List<Integer>> getPersonalItemMeetingTimes(int pitem_id)

    TODO: delete personal item time slots?
     */

    // ######## DEPARTMENTS #######################################################

    /**
     * Insert department into departments table.
     * Duplicate entries (entries with the same dept_code) are ignored.
     * TODO: fill in department names
     * @param dept_code department code e.g. "COMP"
     * @param dept_name department name e.g. "Computer Science"
     * @return dept_id of the inserted department or -1 if insertion fails
     */
    private int insertDepartment(String dept_code, String dept_name) {
        String sql = "INSERT INTO departments (dept_code, dept_name) VALUES (?, ?)" +
                     "ON CONFLICT(dept_code) DO NOTHING";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, dept_code);
            pstmt.setString(2, dept_name);
            pstmt.executeUpdate();
            return getDeptID(dept_code);
        } catch (SQLException e) {
            System.err.println("Failed to insert department: " + e.getMessage());
            return -1;
        }
    }

    /**
     * @param dept_code department code e.g. "COMP"
     * @return dept_id of the specified department or -1 if not found / SQLException occurred
     */
    protected int getDeptID(String dept_code) {
        String sql = """
            SELECT *
            FROM departments
            WHERE dept_code = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, dept_code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("dept_id");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get department id: " + e.getMessage());
        }
        return -1;
    }

    /**
     * @param dept_id FK references departments(dept_id)
     * @return dept_code of the specified department; null if not found or SQLException occurred
     */
    protected String getDeptCode(int dept_id) {
        String sql = """
            SELECT *
            FROM departments
            WHERE dept_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dept_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("dept_code");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get department code: " + e.getMessage());
        }
        return null;
    }

    // ######## COURSES ###########################################################

    /**
     * Insert course into courses table.
     * @param credits       number of credit hours e.g. 3
     * @param is_lab        T if course is a lab; F otherwise
     * @param is_open       T if course is open (open_seats != 0); F otherwise
     * @param location      course location e.g. "STEM 326"
     * @param course_name   course name e.g. "SOFTWARE ENGINEERING"
     * @param course_number number after dept_code e.g. 350
     * @param open_seats    number of open seats e.g. 9
     * @param section_id    section letter e.g. "A" or "B"
     * @param semester      semester course is offered e.g. "2024_Spring"
     * @param dept_id       placeholder for dept_code / FK referencing departments(dept_id)
     * @param total_seats   number of total seats e.g. 23
     * @return course_id of inserted course or -1 if insertion failed
     */
    private int insertCourse(int credits, boolean is_lab, boolean is_open, String location,
                             String course_name, int course_number, int open_seats,
                             String section_id, String semester, int dept_id,
                             int total_seats) {
        String sql = """
            INSERT INTO courses (credits, is_lab, is_open, location, course_name, course_number,
                open_seats, section_id, semester, dept_id, total_seats)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, credits);
            pstmt.setBoolean(2, is_lab);
            pstmt.setBoolean(3, is_open);
            pstmt.setString(4, location);
            pstmt.setString(5, course_name);
            pstmt.setInt(6, course_number);
            pstmt.setInt(7, open_seats);
            pstmt.setString(8, section_id);
            pstmt.setString(9, semester);
            pstmt.setInt(10, dept_id);
            pstmt.setInt(11, total_seats);
            pstmt.executeUpdate();
            return getCourseID(dept_id, course_number, section_id, semester);
        } catch (SQLException e) {
            System.err.println("Failed to insert course: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Given the unique identifiers of a course, return its id.
     * @param dept_id FK referencing departments(dept_id)
     * @param course_number number after dept_code e.g. 350
     * @param section section letter e.g. "A" or "B"
     * @param semester semester course is offered e.g. "2024_Spring"
     * @return course_id or -1 if not found or SQLException thrown
     */
    protected int getCourseID(int dept_id, int course_number, String section,
                              String semester) {
        String sql = """
            SELECT *
            FROM courses
            WHERE dept_id = ? AND course_number = ? AND section_id = ? AND semester = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dept_id);
            pstmt.setInt(2, course_number);
            pstmt.setString(3, section);
            pstmt.setString(4, semester);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("course_id");
            }
            System.err.println("Failed to get course id: course not found");
        } catch (SQLException e) {
            System.err.println("Failed to get course id: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Given a course id, return a CourseItem object representing the table entry.
     * TODO: course db items currently do not contain descriptions!
     * @param course_id FK references courses(course_id)
     * @return CourseItem object or null if entry does not exist / SQLException occurred
     */
    protected CourseItem getCourseByID(int course_id) {
        String sql = """
            SELECT *
            FROM courses
            WHERE course_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, course_id);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                // get dept_code
                String dept_code = getDeptCode(rs.getInt("dept_id"));
                // get professor list
                ArrayList<Professor> professors = getCourseFaculty(course_id);
                // get meeting times
                Map<Character, List<Integer>> meetingTimes = getCourseMeetingTimes(course_id);
                String desc = "TODO: course db items currently do not contain descriptions!";
                /*
                int id, int credits, boolean isLab, String location, String courseName, int courseNumber,
                      char section, String semester, String depCode, String description,
                      ArrayList<Professor> professors, Map<Character, List<Integer>> meetingTimes,
                      boolean onSchedule
                 */
                CourseItem course = new CourseItem(
                        course_id, rs.getInt("credits"), rs.getBoolean("is_lab"),
                        rs.getString("location"), rs.getString("course_name"),
                        rs.getInt("course_number"), rs.getString("section_id").charAt(0),
                        rs.getString("semester"), dept_code, desc, professors,
                        meetingTimes, true
                );
                return course;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user courses: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all courses from the database.
     * @return a list of all CourseItem objects
     */
    protected List<CourseItem> getAllCourses() {
        List<CourseItem> courses = new ArrayList<>();
        String sql = "SELECT courses.*, departments.dept_code " +
                     "FROM courses JOIN departments ON courses.dept_id = departments.dept_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("course_id");
                int credits = rs.getInt("credits");
                boolean isLab = rs.getBoolean("is_lab");

                // Safely retrieve string values, defaulting to an empty string if null
                String location = rs.getString("location");
                location = (location != null) ? location : "";

                String courseName = rs.getString("course_name");
                courseName = (courseName != null) ? courseName : "";

                int courseNumber = rs.getInt("course_number");

                // Safely retrieve section (default to a space if null/empty)
                String sectionStr = rs.getString("section_id");
                char section = (sectionStr != null && !sectionStr.isEmpty()) ? sectionStr.charAt(0) : ' ';

                String semester = rs.getString("semester");
                semester = (semester != null) ? semester : "";

                String depCode = rs.getString("dept_code");  // from join
                depCode = (depCode != null) ? depCode : "";

                // Since there's no 'description' column in the DB, default to an empty string
                String description = "";

                ArrayList<Professor> professors = new ArrayList<>();
                Map<Character, List<Integer>> meetingTimes = new HashMap<>();
                boolean onSchedule = false;

                CourseItem course = new CourseItem(id, credits, isLab, location, courseName, courseNumber,
                        section, semester, depCode, description,
                        professors, meetingTimes, onSchedule);
                courses.add(course);
            }
            System.out.println("Number of courses retrieved: " + courses.size());
        } catch (SQLException e) {
            System.err.println("Error retrieving courses: " + e.getMessage());
        }

        return courses;
    }

    /**
     * returns all course ids in the database
     * @return ArrayList of all course ids
     */
    protected ArrayList<Integer> getAllCourseIds() {
        ArrayList<Integer> courses = new ArrayList<>();
        String sql = "SELECT course_id FROM courses";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                courses.add(Integer.valueOf(rs.getInt("course_id")));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to retrieve all course IDs: " + e.getMessage());
        }
        return courses;
    }

    // ######## FACULTY ###########################################################

    /**
     * Insert faculty member into faculty table.
     * Duplicate entries (entries with the same faculty_name) are ignored.
     * As is, faculty_name must be unique.
     * TODO: avg_rating / avg_difficulty
     * @param faculty_name faculty name e.g. "Hutchins, Jonathan O."
     * @param avg_rating "Overall Quality" rating (default -1)
     * @param avg_difficulty "Level of Difficulty" rating (default -1)
     * @return faculty_id of the inserted faculty member; -1 if insertion failed
     */
    private int insertFaculty(String faculty_name, double avg_rating, double avg_difficulty) {
        String sql = "INSERT INTO faculty (faculty_name, avg_rating, avg_difficulty) VALUES (?, ?, ?) " +
                     "ON CONFLICT(faculty_name) DO NOTHING";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, faculty_name);
            pstmt.setDouble(2, avg_rating);
            pstmt.setDouble(3, avg_difficulty);
            pstmt.executeUpdate();
            return getFacultyID(faculty_name);
        } catch (SQLException e) {
            System.err.println("Failed to insert faculty: " + e.getMessage());
            return -1;
        }
    }

    /**
     * @param faculty_name faculty name e.g. "Hutchins, Jonathan O."
     * @return faculty_id of the specified faculty member or -1 if not found / SQLException occurred
     */
    protected int getFacultyID(String faculty_name) {
        String sql = """
            SELECT *
            FROM faculty
            WHERE faculty_name = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, faculty_name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("faculty_id");
            }
//            System.err.println("Failed to get faculty id: faculty_name not found");
        } catch (SQLException e) {
            System.err.println("Failed to get faculty id: " + e.getMessage());
        }
        return -1;
    }

    /**
     * TODO
     * @param faculty_id FK references faculty(faculty_id)
     * @param newRating "Overall Quality" rating (...)
     * @param newDifficulty "Level of Difficulty" rating (default -1)
     * @return true if rating updated successfully; false otherwise
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    protected boolean updateFacultyRating(int faculty_id, double newRating, double newDifficulty)
            throws SQLException, IllegalArgumentException {

        String tableName = "faculty";

        // Validate the input values
        if (newRating < 0 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        if (newDifficulty < 0 || newDifficulty > 5) {
            throw new IllegalArgumentException("Difficulty must be between 0 and 5");
        }

        int rowsUpdated = 0;
        // Prepare the update statement
        String updateQuery = "UPDATE " + tableName + " SET avg_rating = ?, avg_difficulty = ? WHERE faculty_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            // Set parameters
            stmt.setDouble(1, newRating);
            stmt.setDouble(2, newDifficulty);
            stmt.setInt(3, faculty_id);

            // Execute the update
            rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Successfully updated faculty ID " + faculty_id +
                                   " with rating: " + newRating +
                                   ", difficulty: " + newDifficulty);
                return true;
            } else {
                System.out.println("Faculty ID " + faculty_id + " not found in the database.");
                return false;
            }
        }
    }

    // ######## COURSE_FACULTY ####################################################

    /**
     * Insert (course_id, faculty_id) pair into course_faculty table.
     * @param course_id FK references courses(course_id)
     * @param faculty_id FK references faculty(faculty_id)
     */
    private void insertCourseFaculty(int course_id, int faculty_id) {
        // TODO: check that course_id / faculty_id are valid ids
        String sql = "INSERT INTO course_faculty (course_id, faculty_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, course_id);
            pstmt.setInt(2, faculty_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert course_faculty: " + e.getMessage());
        }
    }

    /**
     * Get list of faculty members associated with a given course.
     * @param course_id FK references courses(course_id)
     * @return ArrayList of Professor objects or null if SQLException occurred
     */
    protected ArrayList<Professor> getCourseFaculty(int course_id) {
        ArrayList<Professor> course_faculty = new ArrayList<>();
        String sql = """
            SELECT *
            FROM course_faculty
            JOIN faculty
            USING(faculty_id)
            WHERE course_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, course_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Professor professor = new Professor(rs.getInt("faculty_id"),
                        rs.getString("faculty_name"), rs.getDouble("avg_rating"),
                        rs.getDouble("avg_difficulty"));
                course_faculty.add(professor);
            }
            return course_faculty;
        } catch (SQLException e) {
            System.err.println("Failed to get course faculty: " + e.getMessage());
            return null;
        }
    }

    // ######## TIME_SLOTS ########################################################

    /**
     * Insert time slot into time_slots table.
     * Duplicate entries (entries with the same day, start, and end) are ignored.
     * @param day e.g. "W"
     * @param start e.g. 1100
     * @param end e.g. 1145
     * @return time_id of inserted time slot or -1 if exception thrown
     */
    private int insertTimeSlot(String day, int start, int end) {
        String sql = "INSERT INTO time_slots (day, start, end) VALUES (?, ?, ?)" +
                     "ON CONFLICT(day, start, end) DO NOTHING";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, day);
            pstmt.setInt(2, start);
            pstmt.setInt(3, end);
            pstmt.executeUpdate();
            return getTimeSlotID(day, start, end);
        } catch (SQLException e) {
            System.err.println("Failed to insert department: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get id of the time slot with entries (day, start, end).
     * @param day e.g. "W"
     * @param start e.g. 1100
     * @param end e.g. 1145
     * @return time_id or -1 if not found or SQLException thrown
     */
    private int getTimeSlotID(String day, int start, int end) {
        String sql = """
            SELECT time_id
            FROM time_slots
            WHERE day = ? AND start = ? AND end = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, day);
            pstmt.setInt(2, start);
            pstmt.setInt(3, end);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("time_id");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get time slot id: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get map ('D', [1100, 200]) from time_id.
     * @param time_id
     * @return
     */
    private Map<Character, List<Integer>> getMeetingTime(int time_id) {
        Map<Character, List<Integer>> meetingTime = new HashMap<>();
        String sql = """
            SELECT *
            FROM time_slots
            WHERE time_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, time_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                meetingTime.put(Character.valueOf(rs.getString("day").charAt(0)),
                        new ArrayList<>(Arrays.asList(rs.getInt("start"),
                                rs.getInt("end"))));
                return meetingTime;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get time slot id: " + e.getMessage());
        }
        return null;
    }

    // ######## COURSE_TIME_SLOTS #################################################

    /**
     * Insert course time slot into pitem_time_slots table.
     * @param time_id
     * @param course_id FK references courses(course_id)
     */
    private void insertCourseTimeSlot(int time_id, int course_id) {
        String sql = "INSERT INTO course_time_slots (time_id, course_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, time_id);
            pstmt.setInt(2, course_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert course time slot: " + e.getMessage());
            System.out.println(course_id + " " + time_id);
        }
    }

    /**
     * Get meeting times of course with specified id.
     * @param course_id FK references courses(course_id)
     * @return
     */
    private Map<Character, List<Integer>> getCourseMeetingTimes(int course_id) {
        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
        String sql = """
            SELECT time_id
            FROM course_time_slots
            WHERE course_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, course_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<Character, List<Integer>> meetingTime = getMeetingTime(rs.getInt("time_id"));
                if (meetingTime != null) {
                    meetingTimes.putAll(meetingTime);
                }
            }
            return meetingTimes;
        } catch (SQLException e) {
            System.err.println("Failed to get course meeting times: " + e.getMessage());
        }
        return null;
    }

    // ######## USERS #############################################################

    /**
     * Insert user into users table.
     * NOTE: password should be hashed before calling this method.
     * @param user_email user email e.g. "proctorhm22@gcc.edu"
     * @param user_password user password e.g. "password"
     * @return user_id of the inserted user or -1 if insertion failed
     */
    protected int insertUser(String user_email, String user_password) {
        String sql = "INSERT INTO users (user_email, user_password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user_email);
            pstmt.setString(2, user_password);
            pstmt.executeUpdate();
            try {
                return getUserID(user_email, user_password);
            } catch (Exception e) {
                System.err.println("Failed to get user_id: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert user: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Returns user_id of the user corresponding to the given email and password.
     * @param user_email user email e.g. "proctorhm22@gcc.edu
     * @param user_password user password e.g. "password"
     * @return user_id of the specified user or -1 if not found or SQLException thrown
     */
    protected int getUserID(String user_email, String user_password) {
        String sql = """
            SELECT *
            FROM users
            WHERE user_email = ? AND user_password = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user_email);
            pstmt.setString(2, user_password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user from database: " + e.getMessage());
        }
        return -1;
    }

    /**
     * TODO
     * @param user_id
     * @param new_email
     */
    protected void updateUserEmail(int user_id, String new_email) {
        String sql = "UPDATE users SET user_email = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, new_email);
            pstmt.setInt(2, user_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update user email: " + e.getMessage());
        }
    }

    /**
     * TODO
     * @param user_id
     * @param new_password
     */
    protected void updateUserPassword(int user_id, String new_password) {
        String sql = "UPDATE users SET user_password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, new_password); // Consider hashing in production
            stmt.setInt(2, user_id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update user password: " + e.getMessage());
        }
    }

    /**
     * TODO: delete other data first!
     * @param user_id
     */
    protected void deleteUser(int user_id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete user: " + e.getMessage());
        }
        for (Schedule schedule : getAllUserSchedules(user_id)) {
            deleteUserSchedule(user_id, schedule.getID());
        }
    }

    // ######## USER_SCHEDULES ####################################################

    /**
     * TODO
     * @param user_id
     * @param sched_name
     * @return
     */
    protected int insertUserSchedule(int user_id, String sched_name) {
        String sql = "INSERT INTO user_schedules (user_id, sched_name) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, user_id);
            stmt.setString(2, sched_name);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to create user schedule: " + e.getMessage());
        }
        return -1;
    }

    /**
     * TODO
     * @param user_id
     * @param sched_name
     * @return
     */
    protected int getScheduleID(int user_id, String sched_name) {
        String sql = "SELECT sched_id FROM user_schedules WHERE user_id = ? AND sched_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setString(2, sched_name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("sched_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get schedule id: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Returns an array list of courses / personal items associated with the given schedule id.
     * @param sched_id
     * @return
     */
    protected ArrayList<ScheduleItem> getScheduleItems(int sched_id) {
        ArrayList<ScheduleItem> schedule = new ArrayList<>();
        schedule.addAll(getScheduleCourses(sched_id));
        schedule.addAll(getSchedulePersonalItems(sched_id));
        return schedule;
    }

    /**
     * TODO
     * @param user_id
     * @param sched_id
     * @return Schedule object or null if schedule does not exist.
     */
    protected Schedule getSchduleByID(int user_id, int sched_id) {
        String sql = "SELECT sched_name FROM user_schedules WHERE user_id = ? AND sched_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, sched_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("sched_name");
                    Schedule schedule = new Schedule(sched_id, name);
                    schedule.setScheduleItems(getScheduleItems(sched_id));
                    return schedule;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get user schedule: " + e.getMessage());
        }
        return null;
    }

    /**
     * TODO
     * @param user_id
     * @return
     */
    protected ArrayList<Schedule> getAllUserSchedules(int user_id) {
        ArrayList<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT sched_id FROM user_schedules WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int schedId = rs.getInt("sched_id");
                    schedules.add(getSchduleByID(user_id, schedId));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get list of user schedules: " + e.getMessage());
        }
        return schedules;
    }


    /**
     * TODO
     * @param user_id
     * @param sched_id
     */
    protected void deleteUserSchedule(int user_id, int sched_id) {
        for (ScheduleItem item : getScheduleItems(sched_id)) {
            if (item instanceof CourseItem) {
                removeCourseFromSchedule(sched_id, item.getId());
            }
            else {
                removePersonalItemFromSchedule(sched_id, item.getId());
            }
        }
        String sql = "DELETE FROM user_schedules WHERE user_id = ? AND sched_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, sched_id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to delete user schedule: " + e.getMessage());
        }
    }

    // ######## USER_COURSES ######################################################

    /**
     * Add course to specified schedule.
     * @param sched_id
     * @param course_id
     */
    protected void addCourseToSchedule(int sched_id, int course_id) {
        // TODO: validate course_id / sched_id exist
        String sql = "INSERT INTO user_courses (sched_id, course_id) VALUES (?, ?) "
                     + "ON CONFLICT(sched_id, course_id) DO NOTHING";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            pstmt.setInt(2, course_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add course to schedule: " + e.getMessage());
        }
    }

    /**
     * Remove course from specified schedule.
     * @param sched_id
     * @param course_id
     */
    protected void removeCourseFromSchedule(int sched_id, int course_id) {
        String sql = "DELETE FROM user_courses WHERE sched_id = ? AND course_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            pstmt.setInt(2, course_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to remove course from schedule: " + e.getMessage());
        }
    }

    /**
     * Returns an ArrayList of the courses associated with the given schedule.
     * @param sched_id
     * @return
     */
    protected ArrayList<CourseItem> getScheduleCourses(int sched_id) {
        ArrayList<CourseItem> courses = new ArrayList<>();
        String sql = """
            SELECT course_id
            FROM user_courses
            WHERE sched_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int course_id = rs.getInt("course_id");
                courses.add(getCourseByID(course_id));
            }
            return courses;
        } catch (SQLException e) {
            System.out.println("Failed to get schedule courses: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns the list of course IDs in the specified schedule.
     */
    protected List<Integer> getScheduleCourseIds(int sched_id) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT course_id FROM user_courses WHERE sched_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sched_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(Integer.valueOf(rs.getInt("course_id")));
                }
            }
            return ids;
        } catch (SQLException e) {
            System.err.println("Error fetching course ids: " + e.getMessage());
        }
        return null;
    }

    /**
     * TODO
     * @param sched_id
     * @param courseId
     * @return
     */
    protected boolean courseInSchedule(int sched_id, int courseId) {
        String sql = "SELECT 1 FROM user_courses WHERE sched_id = ? AND course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sched_id);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking user course: " + e.getMessage());
            return false;
        }
    }

    // ######## USER_PERSONAL_ITEMS ###############################################

    /**
     * Adds personal item to specified schedule.
     * @param sched_id
     * @param pitem_name personal item name e.g. "Chapel"
     * @param meetingTimes personal item meeting times e.g. {"W":{1100, 1145}}
     * @return pitem_id ocf inserted personal item.
     */
    protected int addPersonalItemToSchedule(int sched_id, String pitem_name, Map<Character,
            List<Integer>> meetingTimes) {
        String sql = "INSERT INTO user_personal_items (sched_id, pitem_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            pstmt.setString(2, pitem_name);
            pstmt.executeUpdate();
            int pitem_id = getPersonalItemID(sched_id, pitem_name);
            // link to time-slots table
            for (Map.Entry<Character, List<Integer>> entry : meetingTimes.entrySet()) {
                String day = "" + entry.getKey();
                int start = entry.getValue().get(0);
                int end = entry.getValue().get(1);
                int time_id = getTimeSlotID(day, start, end);
                if (time_id == -1) {
                    time_id = insertTimeSlot(day, start, end);
                }
                insertPersonalItemTimeSlot(time_id, pitem_id);
            }
            return pitem_id;
        } catch (SQLException e) {
            System.err.println("Failed to insert personal item: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Returns id of the personal item associated with the given schedule / pitem_name.
     * @param sched_id
     * @param pitem_name
     * @return
     */
    protected int getPersonalItemID(int sched_id, String pitem_name) {
        String sql = """
            SELECT pitem_id
            FROM user_personal_items
            WHERE sched_id = ? AND pitem_name = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            pstmt.setString(2, pitem_name);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getInt("pitem_id");
            }
            System.out.println("ERROR: failed to get pitem id: pitem not found");
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get pitem id: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Returns ScheduleItem object of the personal item associated with the given id.
     * @param pitem_id
     * @return
     */
    protected ScheduleItem getPersonalItemByID(int pitem_id) {
        String sql = """
            SELECT *
            FROM user_personal_items
            WHERE pitem_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, pitem_id);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                // get meeting times
                Map<Character, List<Integer>> meetingTimes = getPersonalItemMeetingTimes(pitem_id);
                return new ScheduleItem(pitem_id, rs.getString("pitem_name"),
                        meetingTimes);
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user courses: " + e.getMessage());
        }
        return null;
    }

    /**
     * remove personal item from user's schedule
     * @param sched_id
     * @param pitem_id
     */
    protected void removePersonalItemFromSchedule(int sched_id, int pitem_id) {
        // TODO: check that user_id / pitem_id are valid ids
        String sql = "DELETE FROM user_personal_items WHERE sched_id = ? AND pitem_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            pstmt.setInt(2, pitem_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERROR: failed to delete personal item: " + e.getMessage());
        }
    }

    /**
     * TODO
     * @param sched_id
     * @return ArrayList of personal items associated with the given schedule.
     */
    protected ArrayList<ScheduleItem> getSchedulePersonalItems(int sched_id) {
        ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();
        String sql = """
            SELECT pitem_id
            FROM user_personal_items
            WHERE sched_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sched_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int pitem_id = rs.getInt("pitem_id");
                ScheduleItem item = getPersonalItemByID(pitem_id);
                scheduleItems.add(item);
            }
            return scheduleItems;
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get schedule personal items: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // ######## PITEM_TIME_SLOTS ##################################################

    /**
     * Insert personal item time slot into pitem_time_slots table.
     * @param time_id
     * @param pitem_id FK references user_personal_items(pitem_id)
     */
    private void insertPersonalItemTimeSlot(int time_id, int pitem_id) {
        String sql = "INSERT INTO pitem_time_slots (time_id, pitem_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, time_id);
            pstmt.setInt(2, pitem_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert personal item time slot: " + e.getMessage());
        }
    }

    /**
     * Get meeting times of personal item with specified id.
     * @param pitem_id FK references user_personal_items(pitem_id)
     * @return
     */
    private Map<Character, List<Integer>> getPersonalItemMeetingTimes(int pitem_id) {
        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
        String sql = """
            SELECT time_id
            FROM pitem_time_slots
            WHERE pitem_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, pitem_id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<Character, List<Integer>> meetingTime = getMeetingTime(rs.getInt("time_id"));
                if (meetingTime != null) {
                    meetingTimes.putAll(meetingTime);
                }
            }
            return meetingTimes;
        } catch (SQLException e) {
            System.err.println("Failed to get personal item meeting times: " + e.getMessage());
        }
        return null;
    }


    // ############################################################################
    // SEARCH METHODS
    // ############################################################################

    /**
     * returns a list of course IDs for all courses that contain the keyword
     * @param keyword
     * @return a list of course IDs
     */
    protected ArrayList<Integer> searchCoursesByKeyword(String keyword) {
        ArrayList<Integer> courses = new ArrayList<>();
        String sql = """
            SELECT course_id
            FROM courses
            WHERE course_name LIKE ?
               OR location LIKE ?
               OR course_number LIKE ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%"; // Wildcard search
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                courses.add(Integer.valueOf(rs.getInt("course_id")));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to search courses: " + e.getMessage());
        }
        return courses;
    }

    /**
     * TODO
     * @param keyword
     * @return
     */
    protected ArrayList<Integer> searchCoursesFuzzy(String keyword) {
        ArrayList<Integer> courses = new ArrayList<>();
        String sql = """
        SELECT course_id 
        FROM courses
        WHERE course_name LIKE ? 
           OR location LIKE ? 
           OR course_number LIKE ?
        ORDER BY LENGTH(course_name) - LENGTH(REPLACE(LOWER(course_name), LOWER(?), '')) ASC
        LIMIT 5
    """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%"; // Wildcard search
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, keyword.toLowerCase());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                courses.add(Integer.valueOf(rs.getInt("course_id")));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to search courses: " + e.getMessage());
        }
        return courses;
    }


    // ############################################################################
    // EXAMPLE USAGE/ TESTING
    // ############################################################################

    // TODO: comment out method
    public static void main(String[] args) {

       NewDatabaseManager dm = new NewDatabaseManager();

        System.out.println("\n" + dm.getCourseCount() + " courses loaded from JSON");

        String email1 = "example_1@gcc.edu";
        String email2 = "example_2@gcc.edu";
        String password = "password";

        // add example users to db
        dm.insertUser(email1, password);
        dm.insertUser(email2, password);

        // test login (1)
        System.out.println("\nTEST LOGIN (1)");
        System.out.println("get " + email1 + " (correct password): " + dm.getUserID(email1, password));
        System.out.println("get " + email1 + " (incorrect password): " + dm.getUserID(email1, "bad password"));

        // test login (2)
        System.out.println("\nTEST LOGIN (2)");
        System.out.println("get " + email2 + " (correct password): " + dm.getUserID(email2, password));
        System.out.println("get " + email2 + " (incorrect password): " + dm.getUserID(email2, "bad password"));

        // test login (3)
        System.out.println("\nTEST LOGIN (3)");
        System.out.println("invalid email: " + dm.getUserID("bad email", password));

        // get info - SOFTWARE ENGINEERING (db calls)
        System.out.println("\nTEST COURSE INFO (1)");
        System.out.println("course: " + dm.getCourseByID(925));
        System.out.println("course faculty: " + dm.getCourseFaculty(925));
        System.out.println("course meeting times: " + dm.getCourseMeetingTimes(925));

        // get info - SOFTWARE ENGINEERING (class calls)
        System.out.println("\nTEST COURSE INFO (1.5)");
        CourseItem course = dm.getCourseByID(925);
        System.out.println("course: " + course);
        System.out.println("course faculty: " + course.getProfessors());
        System.out.println("course meeting times: " + course.getMeetingTimes());

        // get info - MECHANICAL SYSTEMS LAB (multiple professors)
        System.out.println("\nTEST COURSE INFO (2)");
        System.out.println("course: " + dm.getCourseByID(424));
        System.out.println("course faculty: " + dm.getCourseFaculty(424));
        System.out.println("course meeting times: " + dm.getCourseMeetingTimes(424));

        // "login" as user 1
        int user_id = dm.getUserID(email1, password);
        System.out.println("\n[logged in as  " + email1 + " / " + user_id + "]");

        // test get all schedules
        System.out.println("\nTEST GET ALL SCHEDULES (1)");
        System.out.println(dm.getAllUserSchedules(user_id));

        // test adding schedules
        System.out.println("\nTEST ADD SCHEDULE (1)");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.insertUserSchedule(user_id, "EX1");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.insertUserSchedule(user_id, "EX2");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.insertUserSchedule(user_id, "EX3");
        System.out.println(dm.getAllUserSchedules(user_id));

        // test deleting schedule
        System.out.println("\nTEST DELETE SCHEDULE (1)");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.deleteUserSchedule(user_id, dm.getScheduleID(user_id, "EX3"));
        System.out.println(dm.getAllUserSchedules(user_id));

        int sched_id = dm.getScheduleID(user_id, "EX1");

        // add courses to user schedule
        System.out.println("\nTEST ADDING COURSES TO SCHEDULE (1)");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.addCourseToSchedule(sched_id, 925); // SOFTWARE ENGINEERING
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.addCourseToSchedule(sched_id, 424); // MECHANICAL SYSTEMS LAB
        System.out.println(dm.getAllUserSchedules(user_id));

        // remove course from user schedule
        System.out.println("\nTEST REMOVING COURSE FROM SCHEDULE (1)");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.removeCourseFromSchedule(sched_id, 424); // MECHANICAL SYSTEMS LAB
        System.out.println(dm.getAllUserSchedules(user_id));

        // add personal items to user schedule
        System.out.println("\nTEST ADDING USER PERSONAL ITEMS (1)");
        System.out.println(dm.getAllUserSchedules(user_id));
        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
        meetingTimes.put(Character.valueOf('W'), new ArrayList<>(Arrays.asList(1100, 1145)));
        dm.addPersonalItemToSchedule(sched_id, "Chapel", meetingTimes);
        System.out.println(dm.getAllUserSchedules(user_id));
        meetingTimes.clear();
        meetingTimes.put(Character.valueOf('M'), new ArrayList<>(Arrays.asList(1100, 1145)));
        meetingTimes.put(Character.valueOf('F'), new ArrayList<>(Arrays.asList(1100, 1145)));
        dm.addPersonalItemToSchedule(sched_id, "Lunch", meetingTimes);
        System.out.println(dm.getAllUserSchedules(user_id));

        // get info - "Lunch" (db calls)
        System.out.println("\nTEST PERSONAL ITEM INFO (1)");
        int pitem_id = dm.getPersonalItemID(user_id, "Chapel");
        System.out.println("item: " + dm.getPersonalItemByID(pitem_id));
        System.out.println("item meeting times: " + dm.getPersonalItemMeetingTimes(pitem_id));

        // get info - "Lunch" (db calls)
        System.out.println("\nTEST PERSONAL ITEM INFO (2)");
        pitem_id = dm.getPersonalItemID(user_id, "Lunch");
        System.out.println("item: " + dm.getPersonalItemByID(pitem_id));
        System.out.println("item meeting times: " + dm.getPersonalItemMeetingTimes(pitem_id));

        // remove personal item from user schedule
        System.out.println("\nTEST REMOVING USER PERSONAL ITEM (1)");
        System.out.println(dm.getAllUserSchedules(user_id));
        dm.removePersonalItemFromSchedule(sched_id, pitem_id); // "Lunch"
        System.out.println(dm.getAllUserSchedules(user_id));

        // test deleting user
        System.out.println("\nTEST DELETING USER (1)");
        dm.deleteUser(user_id);
        System.out.println(dm.getUserID(email1, password));
        System.out.println(dm.getAllUserSchedules(user_id));
        System.out.println(dm.getScheduleCourses(sched_id));

        dm.close();

    }

}