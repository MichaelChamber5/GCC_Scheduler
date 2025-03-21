package edu.gcc.BitwiseWizards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private Connection connection = null;
    private static final String DB_URL = "jdbc:sqlite:GCC_Scheduler.db";
    private static final String JSON_FILE = "/data_wolfe.json"; // download / move to resources folder

    /**
     * Connects to and initializes database.
     */
    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("\nSuccessfully connected to database.");
            initializeDatabase();
        } catch(SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Connects to but DOES NOT initialize database.
     * TODO: better way to incorporate search / DatabaseManager?
     * @param dbm database manager
     */
    public DatabaseManager(DatabaseManager dbm) {
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch(SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Creates tables and fills them with dummy data.
     * TODO: ideally only run dropTables() once before commenting it out.. but its good for testing
     */
    private void initializeDatabase() {
        try {
            //dropTables();
            createTables();
            populateTables();
            System.out.println("Successfully initialized database.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
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
                DROP TABLE IF EXISTS personal_items;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS users;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS user_courses;
            """);
            // TODO: drop personal item times from time_slots table?
            stmt.execute("""
                DROP TABLE IF EXISTS pitem_time_slots;
            """);
        }
    }

    /**
     * Creates the following tables:
     *      departments         (dept_id, dept_code, dept_name)
     *      faculty             (faculty_id, faculty_name, avg_rating, avg_difficulty)
     *      courses             (course_id, credits, is_lab, is_open, location, ...)
     *      course_faculty      (course_id, faculty_id)
     *      personal_items      (pitem_id, pitem_name)
     *      users               (user_id, user_email, user_password)
     *      user_courses        (user_id, course_id)
     *      time_slots          (time_id, day, start, end)
     *      course_time_slots   (course_id, time_id)
     *      pitem_time_slots    (pitem_id, time_id)
     * @throws SQLException if commands fail
     */
    private void createTables() throws SQLException {
        // Example JSON entry:
        /*
            {   "credits":3,
                "faculty":["Hutchins, Jonathan O."],
                "is_lab":false,
                "is_open":true,
                "location":"STEM 326",
                "name":"SOFTWARE ENGINEERING",
                "number":350,
                "open_seats":9,
                "section":"B",
                "semester":"2024_Spring",
                "subject":"COMP",
                "times":[   {   "day":"M",
                                "end_time":"15:50:00",
                                "start_time":"15:00:00"
                            },
                            {   "day":"W",
                                "end_time":"15:50:00",
                                "start_time":"15:00:00"
                            },
                            {   "day":"F",
                                "end_time":"15:50:00",
                                "start_time":"15:00:00"
                            }
                        ],
                "total_seats":23
            }
         */
        try (Statement stmt = connection.createStatement()) {
            // departments table (dept_id, "COMP", "Computer Science")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    dept_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    dept_code TEXT NOT NULL UNIQUE,
                    dept_name TEXT
                )
            """);
            // faculty table (faculty_id, "Hutchins, Jonathan O.", 4.3, 2.5)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faculty (
                    faculty_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    faculty_name TEXT NOT NULL UNIQUE,
                    avg_rating REAL,
                    avg_difficulty REAL
                )
            """);
            // courses table (course_id, "SOFTWARE ENGINEERING", "COMP", 350, "B", "2024_Spring")
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
            // courses x faculty table (course_id, faculty_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS course_faculty (
                    course_id INTEGER NOT NULL,
                    faculty_id INTEGER NOT NULL,
                    PRIMARY KEY (course_id, faculty_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id),
                    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
                )
            """);
            // personal_items table (pitem_id, "Chapel")
            // TODO: pitem_id / unique(user_id, pitem_name) issues
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS personal_items (
                    pitem_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    pitem_name TEXT NOT NULL,
                    UNIQUE(user_id, pitem_name),
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
            """);
            // users table (user_id, "proctorhm22@gcc.edu", "password")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_email TEXT NOT NULL UNIQUE,
                    user_password TEXT NOT NULL
                )
            """);
            // users x courses table (user_id, course_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_courses (
                    user_id INTEGER NOT NULL,
                    course_id INTEGER NOT NULL,
                    PRIMARY KEY (user_id, course_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id)
                )
            """);
            // time slots table (time_id, 'W', 1100, 1145)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS time_slots (
                    time_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    day TEXT NOT NULL,
                    start INTEGER NOT NULL,
                    end INTEGER NOT NULL,
                    UNIQUE(day, start, end)
                )
            """);
            // course_time_slots table (course_id, time_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS course_time_slots (
                    course_id INTEGER NOT NULL,
                    time_id INTEGER NOT NULL,
                    PRIMARY KEY (course_id, time_id),
                    FOREIGN KEY (course_id) REFERENCES courses(course_id),
                    FOREIGN KEY (time_id) REFERENCES time_slots(time_id)
                )
            """);
            // pitem_time_slots table (pitem_id, time_id)
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

    /**
     * Fills tables with course catalogue data.
     * TODO: fill with dummy user data?
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

    // helper methods for safely getting values from JsonNode

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private int getIntValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asInt() : 0;
    }

    // DEPARTMENTS TABLE INSERT / GET methods

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
//            System.err.println("Failed to get department id: dept_code not found");
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
//            System.err.println("Failed to get department code: dept_id not found");
        } catch (SQLException e) {
            System.err.println("Failed to get department code: " + e.getMessage());
        }
        return null;
    }

    // FACULTY TABLE INSERT / GET methods

    /**
     * Insert faculty member into faculty table.
     * Duplicate entries (entries with the same faculty_name) are ignored.
     * TODO: as is, faculty_name must be unique.
     * TODO: get avg_rating / avg_difficulty from RateMyProfessor API.
     * @param faculty_name faculty name e.g. "Hutchins, Jonathan O."
     * @param avg_rating "Overall Quality" rating on RateMyProfessor (default -1)
     * @param avg_difficulty "Level of Difficulty" rating on RateMyProfessor (default -1)
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

    // COURSES TABLE INSERT / GET methods

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
                             String section_id, String semester, int dept_id, int total_seats) {
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
    protected int getCourseID(int dept_id, int course_number, String section, String semester) {
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
                //TODO: course db items currently do not contain descriptions!
                String desc = "This class stinks :(";
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

    // COURSE_FACULTY TABLE INSERT / GET methods

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

    // PERSONAL_ITEMS TABLE INSERT / GET / DELETE methods

    /**
     * Adds personal item to personal_items table / user's schedule.
     * @param user_id FK references users(user_id)
     * @param pitem_name personal item name e.g. "Chapel"
     * @param meetingTimes personal item meeting times e.g. {"W":{1100, 1145}}
     * @return pitem_id ocf inserted personal item.
     */
    protected int insertPersonalItem(int user_id, String pitem_name, Map<Character, List<Integer>> meetingTimes) {
        String sql = "INSERT INTO personal_items (user_id, pitem_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setString(2, pitem_name);
            pstmt.executeUpdate();
            int pitem_id = getPersonalItemID(user_id, pitem_name);
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
     * Returns id of the personal item associated with the given user / pitem_name.
     * @param user_id FK references users(user_id)
     * @param pitem_name
     * @return
     */
    protected int getPersonalItemID(int user_id, String pitem_name) {
        String sql = """
            SELECT *
            FROM personal_items
            WHERE user_id = ? AND pitem_name = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
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
            FROM personal_items
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

    // TODO: updatePersonalItem()?

    // USERS TABLE INSERT / GET methods

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
     * Use user_id to get user schedule (getUserCourses() / getUserPersonalItems() / getUserSchedule()).
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
     * Returns an array list of courses / personal items associated with the given user.
     * @param user_id FK references users(user_id)
     * @return
     */
    protected ArrayList<ScheduleItem> getUserSchedule(int user_id) {
        ArrayList<ScheduleItem> schedule = new ArrayList<>();
        schedule.addAll(getUserCourses(user_id));
        schedule.addAll(getUserPersonalItems(user_id));
        return schedule;
    }

    // USER_COURSES TABLE INSERT / DELETE / GET methods

    /**
     * Add course to user's schedule.
     * @param user_id FK references users(user_id)
     * @param course_id FK references courses(course_id)
     */
    protected void insertUserCourse(int user_id, int course_id) {
        // TODO: check that course_id is valid id
        String sql = "INSERT INTO user_courses (user_id, course_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, course_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERROR: failed to insert user_course: " + e.getMessage());
        }
    }

    /**
     * Remove course from user's schedule.
     * @param user_id FK references users(user_id)
     * @param course_id FK references courses(course_id)
     */
    protected void deleteUserCourse(int user_id, int course_id) {
        // TODO: check that course_id is valid id
        String sql = "DELETE FROM user_courses WHERE user_id = ? AND course_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, course_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERROR: failed to delete user_course: " + e.getMessage());
        }
    }

    /**
     * Returns an ArrayList of the courses associated with the given user.
     * @param user_id FK references users(user_id)
     * @return
     */
    protected ArrayList<CourseItem> getUserCourses(int user_id) {
        ArrayList<CourseItem> courses = new ArrayList<>();
        String sql = """
            SELECT course_id
            FROM user_courses
            WHERE user_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int course_id = rs.getInt("course_id");
                courses.add(getCourseByID(course_id));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user courses: " + e.getMessage());
        }
        return courses;
    }

    // "USER_PITEMS" DELETE / GET methods

    /**
     * remove personal item from user's schedule
     * @param user_id FK references users(user_id)
     * @param pitem_id
     */
    protected void deleteUserPersonalItem(int user_id, int pitem_id) {
        // TODO: check that user_id / pitem_id are valid ids
        String sql = "DELETE FROM personal_items WHERE user_id = ? AND pitem_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, pitem_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERROR: failed to delete personal item: " + e.getMessage());
        }
    }

    /**
     * @param user_id FK references users(user_id)
     * @return ArrayList of personal items associated with the given user.
     */
    protected ArrayList<ScheduleItem> getUserPersonalItems(int user_id) {
        ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();
        String sql = """
            SELECT pitem_id
            FROM personal_items
            WHERE user_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int pitem_id = rs.getInt("pitem_id");
                ScheduleItem item = getPersonalItemByID(pitem_id);
                scheduleItems.add(item);
            }
            return scheduleItems;
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user personal items: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // TIME_SLOTS TABLE INSERT / GET methods

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
     * ...
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
                meetingTime.put(rs.getString("day").charAt(0),
                        new ArrayList<>(Arrays.asList(rs.getInt("start"),
                                rs.getInt("end"))));
                return meetingTime;
            }
        } catch (SQLException e) {
            System.err.println("Failed to get time slot id: " + e.getMessage());
        }
        return null;
    }

    // TODO: deleteTimeSlot(time_id)

    // COURSE_TIME_SLOTS TABLE INSERT / GET methods

    /**
     * ...
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
     * ...
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

    // PITEM_TIME_SLOTS TABLE INSERT / GET methods

    /**
     * ...
     * @param time_id
     * @param pitem_id
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
     * ...
     * @param pitem_id
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

    // SEARCH METHODS

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
                courses.add(rs.getInt("course_id"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to search courses: " + e.getMessage());
        }
        return courses;
    }
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
                courses.add(rs.getInt("course_id"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to search courses: " + e.getMessage());
        }
        return courses;
    }
    public void addCourse(CourseItem course) {
        // Add the course to your database or in-memory storage
    }


    /**
     * Close database connection.
     */
    protected void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Failed to close database connection: " + e.getMessage());
        }
    }
    /**
     * Retrieves all courses from the database.
     * @return a list of all CourseItem objects
     */
    public List<CourseItem> getAllCourses() {
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

    public boolean isUserCourse(int userId, int courseId) {
        String sql = "SELECT 1 FROM user_courses WHERE user_id = ? AND course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking user course: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the list of course IDs that the specified user has added to their schedule.
     */
    public List<Integer> getUserCourseIds(int userId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT course_id FROM user_courses WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("course_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user courses: " + e.getMessage());
        }
        return ids;
    }
    // testing...
    // TODO: delete method
    public static void main(String[] args) {

        DatabaseManager dm = new DatabaseManager();

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

        // "login" as user 1
        int user_id = dm.getUserID(email1, password);

        // get info - SOFTWARE ENGINEERING (db calls)
        System.out.println("\nTEST COURSE INFO (1)");
        System.out.println("course: " + dm.getCourseByID(925));
        System.out.println("course faculty: " + dm.getCourseFaculty(925));
        System.out.println("course meeting times: " + dm.getCourseMeetingTimes(925));

        // get info - SOFTWARE ENGINEERING (class calls)
//        System.out.println("\nTEST COURSE INFO (1.5)");
//        CourseItem course = dm.getCourseByID(925);
//        System.out.println("course: " + course);
//        System.out.println("course faculty: " + course.getProfessors());
//        System.out.println("course meeting times: " + course.getMeetingTimes());

        // get info - MECHANICAL SYSTEMS LAB (multiple professors)
        System.out.println("\nTEST COURSE INFO (2)");
        System.out.println("course: " + dm.getCourseByID(424));
        System.out.println("course faculty: " + dm.getCourseFaculty(424));
        System.out.println("course meeting times: " + dm.getCourseMeetingTimes(424));

        // add courses to user schedule
        System.out.println("\nTEST ADDING USER COURSES");
        System.out.println(dm.getUserCourses(user_id));
        dm.insertUserCourse(user_id, 925); // software engineering
        System.out.println(dm.getUserCourses(user_id));
        dm.insertUserCourse(user_id, 424); // MECHANICAL SYSTEMS LAB
        System.out.println(dm.getUserCourses(user_id));

        // remove course from user schedule
        System.out.println("\nTEST REMOVING USER COURSE");
        System.out.println(dm.getUserCourses(user_id));
        dm.deleteUserCourse(user_id, 424); // MECHANICAL SYSTEMS LAB
        System.out.println(dm.getUserCourses(user_id));

        // add personal items to user schedule
        System.out.println("\nTEST ADDING USER PERSONAL ITEMS");
        System.out.println(dm.getUserPersonalItems(user_id));
        Map<Character, List<Integer>> meetingTimes = new HashMap<>();
        meetingTimes.put('W', new ArrayList<>(Arrays.asList(1100, 1145)));
        dm.insertPersonalItem(user_id, "Chapel", meetingTimes);
        System.out.println(dm.getUserPersonalItems(user_id));
        meetingTimes.clear();
        meetingTimes.put('M', new ArrayList<>(Arrays.asList(1100, 1145)));
        meetingTimes.put('F', new ArrayList<>(Arrays.asList(1100, 1145)));
        dm.insertPersonalItem(user_id, "Lunch", meetingTimes);
        System.out.println(dm.getUserPersonalItems(user_id));

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
        System.out.println("\nTEST REMOVING USER PERSONAL ITEM");
        System.out.println(dm.getUserPersonalItems(user_id));
        dm.deleteUserPersonalItem(user_id, pitem_id); // "Lunch"
        System.out.println(dm.getUserPersonalItems(user_id));

        // TODO: when deleting personal items, also delete the corresponding pitem_time_slot

        // TODO: when deleting users, also delete their personal items / corresponding pitem_time_slots

        // get user schedule
        System.out.println("\nTEST GETTING USER SCHEDULE");
        System.out.println("courses: "+ dm.getUserCourses(user_id));
        System.out.println("personal items: " + dm.getUserPersonalItems(user_id));
        System.out.println("schedule: " + dm.getUserSchedule(user_id));

        dm.close();

    }

    protected ArrayList<Integer> searchAllCourses() {
        ArrayList<Integer> courses = new ArrayList<>();
        String sql = "SELECT course_id FROM courses";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("course_id");

                courses.add(id);
            }
        } catch (SQLException e) {
            System.out.println(" ERROR: failed to fetch all courses: " + e.getMessage());
        }
        return courses;
    }

    //
     //


}