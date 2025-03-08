package edu.gcc.BitwiseWizards;

import java.sql.*;

public class DatabaseManager {

    private Connection connection = null;
    private static final String DB_URL = "jdbc:sqlite:GCC_Scheduler.db";
    private static final String JSON_FILE = "/data_wolfe.json"; // download / move to resources folder


    /**
     * Constructor that connects to / initializes the database.
     */
    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
        } catch(SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Helper / middle method to create tables / fill them with dummy data.
     * TODO: remove drop tables line
     */
    private void initializeDatabase() {
        try {
            dropTables();
            createTables();
            populateTables();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Drops all tables currently in the database (assuming you didn't make other ones).
     * TODO: only run this once before adding the data you want to keep
     * @throws SQLException if commands fail
     */
    public void dropTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // there's probably a more efficient way to do this but
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
        }
    }


    /**
     * Creates the following tables:
     *      departments     (dept_id, dept_code, dept_name)
     *      faculty         (faculty_id, faculty_name, avg_rating, avg_difficulty)
     *      courses         (course_id, course_name, ...)
     *      course_faculty  (course_id, faculty_id)
     *      personal_items  (pitem_id, pitem_name, ...)
     *      users           (user_id, user_email, user_password)
     *      user_courses    (user_id, course_id)
     *      user_pitems     (user_id, pitem_id)
     *      time_slots      (...)
     * @throws SQLException if commands fail
     */
    public void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // departments table (dept_id, "COMP", "Computer Science")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    dept_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    dept_code TEXT NOT NULL UNIQUE,
                    dept_name TEXT
                )
            """);
            // faculty table (faculty_id, "Hutchins, Jonathan O.", avg_rating, avg_difficulty)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faculty (
                    faculty_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    faculty_name TEXT NOT NULL UNIQUE,
                    avg_rating REAL,
                    avg_difficulty REAL
                )
            """);
            // courses table (course_id, "Intro to AI", ...)
            // TODO: finish table... credits, section, is_open, is_lab, etc..
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    course_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_name TEXT NOT NULL
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
            // personal_items table (pitem_id, "Lunch", ...)
            // TODO: finish table...
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
     * Fills tables with dummy data.
     * TODO: fill tables with better dummy data
     */
    private void populateTables() {
        insertDepartment("COMP", "Computer Science");
        insertFaculty("Hutchins, Jonathan O.", 4.3, 2.5);
        insertCourse("Software Engineering");
        insertCourse("Into to AI");
        insertPersonalItem("Lunch");
        insertPersonalItem("Chapel");
        String email = "proctorhm22@gcc.edu";
        String password = "password";
        insertUser(email, password);
    }

    // TODO: get course catalogue data

    /**
     * Insert values into the departments table.
     * @param dept_code e.g. "COMP"
     * @param dept_name e.g. "Computer Science"
     */
    private void insertDepartment(String dept_code, String dept_name) {
        String sql = "INSERT INTO departments (dept_code, dept_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, dept_code);
            pstmt.setString(2, dept_name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert department: " + e.getMessage());
        }
    }

    /**
     * Insert values into the faculty table.
     * @param faculty_name
     * @param avg_rating - RateMyProfessor API
     * @param avg_difficulty - RateMyProfessor API
     */
    private void insertFaculty(String faculty_name, double avg_rating, double avg_difficulty) {
        String sql = "INSERT INTO faculty (faculty_name, avg_rating, avg_difficulty) VALUES (?, ?, ?)";
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
     * Insert values into the courses table.
     * TODO: add other parameters
     * @param course_name
     */
    private void insertCourse(String course_name) {
        String sql = "INSERT INTO courses (course_name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, course_name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert course: " + e.getMessage());
        }
    }

    /**
     * ...
     * @param course_id
     * @param faculty_id
     */
    private void insertCourseFaculty(int course_id, int faculty_id) {
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
     * Inserts values into the personal_items table.
     * TODO: add other parameters
     * @param pitem_name
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
     * Inserts values into the users table.
     * SEE: Main.createUser()
     * @param user_email
     * @param user_password
     */
    protected void insertUser(String user_email, String user_password) {
        String sql = "INSERT INTO users (user_email, user_password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user_email);
            pstmt.setString(2, user_password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert user: " + e.getMessage());
        }
    }

    /**
     * Add course to user schedule.
     * SEE: Main.addScheduleItem()
     * TODO: implement getCourse() to get course_id... or add course_id to CourseItem class
     * @param user_id
     * @param course_id
     */
    protected void insertUserCourse(int user_id, int course_id) {
        String sql = "INSERT INTO user_courses (user_id, course_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, course_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert user_course: " + e.getMessage());
        }
    }

    /**
     * Add personal item to user schedule.
     * SEE: Main.addScheduleItem()
     * TODO: implement getPItem() to get pitem_id... or add pitem_id to PersonalItem class
     * @param user_id
     * @param pitem_id
     */
    protected void insertUserPersonalItem(int user_id, int pitem_id) {
        String sql = "INSERT INTO user_pitems (user_id, pitem_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, pitem_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert user_pitem: " + e.getMessage());
        }
    }

    // TODO: update values (user email / password)

    // TODO: remove values (users table / corresponding entries in other tables)

    // TODO: getUserCourses(int user_id) from user_courses table

    // TODO: getUserPersonalItems(int user_id) from user_pitems table

    /**
     * Identify user in database that corresponds to the given email and password.
     * SEE: Main.login() / getUserCourses() and getUserPersonalItems() above
     * @param user_email
     * @param user_password
     * @return user_id of the corresponding user
     * @throws Exception if email not found or password is incorrect
     */
    public int getUser(String user_email, String user_password) throws Exception {
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
                throw new Exception("Failed to get user: credentials invalid.");
            }
            // else
            return rs.getInt("user_id");
            // TODO: use user_id to get user schedule (user_courses \cup user_pitems)
        } catch (SQLException e) {
            throw new Exception("Failed to get user: " + e.getMessage());
        }
    }

    /**
     * Close database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }

    // for testing
    // TODO: delete method
    public static void main(String[] args) {
        DatabaseManager dm = new DatabaseManager();
        String email = "proctorhm22@gcc.edu";
        String password = "password";
        try {
            System.out.println("User id: " + dm.getUser(email, password));
//            dm.getUser("invalid email", password);
//            dm.getUser(email, "incorrect password");
//            dm.getUser("bad", "bad");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        dm.close();
    }

}
