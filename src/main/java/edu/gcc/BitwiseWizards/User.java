package edu.gcc.BitwiseWizards;

public class User {

    private int user_id;
    private String email;
    private String password_hash;
    private Schedule schedule;

    public User(int user_id, String email, String password) {
        setId(user_id);
        setEmail(email);
        setPassword(password);
        schedule = new Schedule();
    }

    public void setId(int user_id) { // TODO: delete?
        this.user_id = user_id;
    }

    public int getId() {
        return user_id;
    }

    public void setEmail(String email) {
        // TODO: check if valid email
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        // TODO: implement password hashing
        this.password_hash = password;
    }

    // TODO: getPassword()

    public void setSchedule(Schedule schedule) {
//        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    // TODO: can we delete these and just use curr_user.schedule in main?
//
//    public void addCourse(CourseItem item) {
////        this.schedule.addScheduleItem(item);
//    }
//
//    public void addPersonalItem(CourseItem item) {
////        this.schedule.addScheduleItem(item);
//    }
//
//    public void removeCourse(CourseItem item) {
////        this.schedule.removeScheduleItem(item);
//    }
//
//    public void removePersonalItem(CourseItem item) {
////        this.schedule.removeScheduleItem(item);
//    }

    public String toString() {
        String s = email + "\n";
        s = s + "[ ";
        for (ScheduleItem item : schedule.getScheduleItems()) {
            s = s + item + " ";
        }
        s = s + "]";
        return s;
    }

}