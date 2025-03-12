package edu.gcc.BitwiseWizards;

import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;

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
     * Creates tables and fills them with dummy data.
     * TODO: ideally only run dropTables() once before commenting it out.. but its good for testing
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
     * Drops all tables currently in the database (assuming you only create the tables below).
     * @throws SQLException if commands fail
     */
    public void dropTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // there's probably a better way to do this...
            stmt.execute("""
                DROP TABLE IF EXISTS departments;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS faculty;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS courses;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS course_faculty;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS personal_items;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS users;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS user_courses;
            """);
            stmt.execute("""
                DROP TABLE IF EXISTS user_pitems;
            """);
            // TODO: drop time_slots table
        }
    }

    /**
     * Creates the following tables:
     *      departments     (dept_id, dept_code, dept_name)
     *      faculty         (faculty_id, faculty_name, avg_rating, avg_difficulty)
     *      courses         (course_id, credits, is_lab, is_open, location, ...)
     *      course_faculty  (course_id, faculty_id)
     *      personal_items  (pitem_id, pitem_name)
     *      users           (user_id, user_email, user_password)
     *      user_courses    (user_id, course_id)
     *      user_pitems     (user_id, pitem_id)
     *      time_slots      (...)
     * @throws SQLException if commands fail
     */
    public void createTables() throws SQLException {
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
            // TODO: integrate time slots
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
            // TODO: integrate time slots
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS personal_items (
                    pitem_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pitem_name TEXT NOT NULL
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
            // users x personal_items table (user_id, pitem_id)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_pitems (
                    user_id INTEGER NOT NULL,
                    pitem_id INTEGER NOT NULL,
                    PRIMARY KEY (user_id, pitem_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id),
                    FOREIGN KEY (pitem_id) REFERENCES personal_items(pitem_id)
                )
            """);
            // TODO: time_slots table (...)
        }
    }

    /**
     * Fills tables with course catalogue and dummy user data.
     * TODO: fill tables properly.
     */
    private void populateTables() {

        // loadCoursesFromJson();

        insertDepartment("COMP", "Computer Science");
        insertDepartment("COMP", "Computer Science");
        insertDepartment("MATH", "Mathematics");

        insertFaculty("Hutchins, Jonathan O.", 4.3, 2.5);
        insertFaculty("Hutchins, Jonathan O.", 4.3, 2.5);
        insertFaculty("Thompson, Gary L.", 4, 4.2);

        insertCourse(3, false, false, "STEM 326",
                    "SOFTWARE ENGINEERING", 350, 0, "A",
                    "2024_Spring", 1, 23);
        insertCourse(3, false, true, "STEM 326",
                    "SOFTWARE ENGINEERING", 350, 9, "B",
                    "2024_Spring", 1, 23);
        insertCourse(3, false, true, "SHAL 109 Tablet Chairs w/multimedia",
                    "NUMBER THEORY", 422, 4, "A",
                    "2024_Spring", 2, 10);

        insertPersonalItem("Chapel");
        insertPersonalItem("Lunch");

        insertUser("proctorhm22@gcc.edu", "password");
        insertUser("hannahmpro22@gmail.com", "password");

    }

    // TODO: loadCoursesFromJSON()

    // TODO: update values in tables (user email / password... rate my professor api)

    // TODO: remove values from tables (delete user)

    // TODO: modify insert methods to return id of what is inserted

    /**
     * Insert department into departments table.
     * Duplicate entries (entries with the same dept_code) are ignored.
     * @param dept_code department code e.g. "COMP"
     * @param dept_name department name e.g. "Computer Science"
     */
    private void insertDepartment(String dept_code, String dept_name) {
        String sql = "INSERT INTO departments (dept_code, dept_name) VALUES (?, ?) " +
                "ON CONFLICT(dept_code) DO NOTHING";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, dept_code);
            pstmt.setString(2, dept_name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert department: " + e.getMessage());
        }
    }

    /**
     * Insert faculty into faculty table.
     * Duplicate entries (entries with the same faculty_name) are ignored.
     * TODO: as is, faculty_name must be unique.
     * TODO: get avg_rating / avg_difficulty from RateMyProfessor API.
     * @param faculty_name faculty name e.g. "Hutchins, Jonathan O."
     * @param avg_rating "Overall Quality" rating on RateMyProfessor (default -1)
     * @param avg_difficulty "Level of Difficulty" rating on RateMyProfessor (default -1)
     */
    private void insertFaculty(String faculty_name, double avg_rating, double avg_difficulty) {
        String sql = "INSERT INTO faculty (faculty_name, avg_rating, avg_difficulty) VALUES (?, ?, ?) " +
                "ON CONFLICT(faculty_name) DO NOTHING";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, faculty_name);
            pstmt.setDouble(2, avg_rating);
            pstmt.setDouble(3, avg_difficulty);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert faculty: " + e.getMessage());
        }
    }

    /**
     * Insert course into courses table.
     * TODO: link to faculty table.
     * TODO: link to departments table.
     * TODO: link to time_slots table.
     * @param credits       number of credit hours e.g. 3
     * @param is_lab        T if course is a lab; F otherwise
     * @param is_open       T if course is open (open_seats != 0); F otherwise
     * @param location      course location e.g. "STEM 326"
     * @param course_name   course name e.g. "SOFTWARE ENGINEERING"
     * @param course_number number after dept_code e.g. 350
     * @param open_seats    number of open seats e.g. 9
     * @param section_id    section letter e.g. "A" or "B"
     * @param semester      semester course is available e.g. "2024_Spring"
     * @param dept_id       placeholder for dept_code / FK referencing departments(dept_id)
     * @param total_seats   number of total seats e.g. 23
     */
    private void insertCourse(int credits, boolean is_lab, boolean is_open, String location,
                              String course_name, int course_number, int open_seats,
                              String section_id, String semester, int dept_id, int total_seats) {
        String sql = "INSERT INTO courses (credits, is_lab, is_open, location, course_name, " +
                "course_number, open_seats, section_id, semester, dept_id, total_seats) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        } catch (SQLException e) {
            System.err.println("Failed to insert course: " + e.getMessage());
        }
    }

    /**
     * Insert (course_id, faculty_id) pair into course_faculty table.
     * Group by course_id to get list of faculty members associated with the given course.
     * @param course_id FK references courses(course_id)
     * @param faculty_id FK references faculty(faculty_id)
     */
    private void insertCourseFaculty(int course_id, int faculty_id) {
        // TODO: check if course_id / faculty_id are valid ids?
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
     * Insert personal_item into personal_items table.
     * TODO: link to time_slots table.
     * @param pitem_name personal item name e.g. "Chapel"
     */
    protected void insertPersonalItem(String pitem_name) {
        String sql = "INSERT INTO personal_items (pitem_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, pitem_name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert personal item: " + e.getMessage());
        }
    }

    /**
     * Returns user_id of the user corresponding to the given email and password.
     * TODO: modify to return user object.
     * @param user_email user email e.g. "proctorhm22@gcc.edu
     * @param user_password user password e.g. "password"
     * @return user_id of the specified user or -1 if email not found or password is incorrect
     */
    public int getUser(String user_email, String user_password) {
        String sql = """
            SELECT *
            FROM users
            WHERE user_email = ? AND user_password = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user_email);
            pstmt.setString(2, user_password);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                // failed to get user: credentials invalid
                return -1;
            }
            // else
            return rs.getInt("user_id");
            // TODO: use user_id to get user schedule (user_courses \cup user_pitems)
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user from database: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Insert user into users table.
     * NOTE: password should be hashed before calling this method.
     * @param user_email user email e.g. "proctorhm22@gcc.edu"
     * @param user_password user password e.g. "password"
     * @return user_id of the inserted user or -1 if insertion fails
     */
    protected int insertUser(String user_email, String user_password) {
        String sql = "INSERT INTO users (user_email, user_password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user_email);
            pstmt.setString(2, user_password);
            pstmt.executeUpdate();
            try {
                return getUser(user_email, user_password);
            } catch (Exception e) {
                System.err.println("Failed to get user_id: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert user: " + e.getMessage());
        }
        return -1;
    }

    /**
     * TODO: modify to return array list of CourseItem objects
     * @param user_id
     * @return
     */
    public ArrayList<Integer> getUserCourses(int user_id) {
        ArrayList<Integer> courses = new ArrayList<>();
        String sql = """
            SELECT *
            FROM user_courses
            WHERE user_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                courses.add(rs.getInt("course_id"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user courses: " + e.getMessage());
        }
        return courses;
    }

    /**
     * Add course to user's schedule.
     * Group by user_id to get list of courses in user's schedule.
     * TODO: add course_id to CourseItem class.
     * @param user_id FK references users(user_id)
     * @param course_id FK references courses(course_id)
     */
    protected void insertUserCourse(int user_id, int course_id) {
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
     * TODO: modify to return array list of PersonalItem objects
     * @param user_id
     * @return
     */
    public ArrayList<Integer> getUserPersonalItems(int user_id) {
        ArrayList<Integer> courses = new ArrayList<>();
        String sql = """
            SELECT *
            FROM user_pitems
            WHERE user_id = ?
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                courses.add(rs.getInt("pitem_id"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR: failed to get user personal items: " + e.getMessage());
        }
        return courses;
    }

    /**
     * Add personal item to user's schedule.
     * Group by user_id to get list of personal items in user's schedule.
     * TODO: add pitem_id to PersonalItem class.
     * @param user_id FK references users(user_id)
     * @param pitem_id FK references personal_items(pitem_id)
     */
    protected void insertUserPersonalItem(int user_id, int pitem_id) {
        String sql = "INSERT INTO user_pitems (user_id, pitem_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, pitem_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("ERROR: failed to insert user_pitem: " + e.getMessage());
        }
    }

    // TODO: get methods for other tables

    /**
     * Close database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Failed to close database connection: " + e.getMessage());
        }
    }

    // testing...
    // TODO: delete method
    public static void main(String[] args) {

        DatabaseManager dm = new DatabaseManager();

        System.out.println("\nget proctorhm22@gcc.edu (correct password): "
                + dm.getUser("proctorhm22@gcc.edu", "password"));
        System.out.println("get hannahmpro22@gmail.com (correct password): "
                + dm.getUser("hannahmpro22@gmail.com", "password"));
        System.out.println("get proctorhm22@gcc.edu (incorrect password): "
                + dm.getUser("proctorhm22@gcc.edu", "bad password"));
        System.out.println("invalid email: " + dm.getUser("bad email", "password"));

        int user_id = dm.getUser("proctorhm22@gcc.edu", "password");

        // add class to user course
        dm.insertUserCourse(user_id, 1);
        dm.insertUserCourse(user_id, 2);

        // add personal item to user course
        dm.insertUserPersonalItem(user_id, 1);

        ArrayList<Integer> user_courses = dm.getUserCourses(user_id);
        ArrayList<Integer> user_pitems = dm.getUserPersonalItems(user_id);

        System.out.println("User courses: "+ user_courses);
        System.out.println("User personal items: " + user_pitems);

        // TODO: modify user courses / pitems so that...

        ArrayList<Integer> user_schedule = new ArrayList<>(user_courses);
        user_schedule.addAll(user_pitems);

        System.out.println("User schedule: " + user_schedule);

        dm.close();

    }

}
