package edu.gcc.BitwiseWizards;

import java.util.List;

public class User {

    private int id;
    private String email;
    private String password_hash;
    private Schedule schedule;

    public User(int user_id, String email, String password) {
        id = user_id;
        setEmail(email);
        setPassword(password);
        schedule = new Schedule();
    }

    public int getId() {
        return id;
    }

    public void setEmail(String email) {
        // TODO: check if valid email
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        // TODO: implement password hashing / check if valid password
        this.password_hash = password;
    }

    public void setSchedule(List<ScheduleItem> items) {
        Schedule new_schedule = new Schedule();
        new_schedule.setScheduleItems(items);
        this.schedule = new_schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public String toString() {
        return email + "\n";
    }

}