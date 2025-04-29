package edu.gcc.BitwiseWizards;

import java.util.ArrayList;
import java.util.List;

public class User {

    private int id;
    private String email;
    private String password_hash;
    private ArrayList<Schedule> schedules;

    public User(int user_id, String email, String password) {
        id = user_id;
        setEmail(email);
        setPassword(password);
        schedules = new ArrayList<>();
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

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = new ArrayList<>(schedules);
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void createNewSchedule(int sched_id, String name) {
        Schedule new_schedule = new Schedule(sched_id, name);
        this.schedules.add(new_schedule);
    }

    public void createNewSchedule(int sched_id, String name, List<ScheduleItem> items) {
        Schedule new_schedule = new Schedule(sched_id, name);
        new_schedule.setScheduleItems(items);
        this.schedules.add(new_schedule);
    }

    public Schedule getSchduleByID(int sched_id) {
        for (Schedule schedule : schedules) {
            if (schedule.getID() == sched_id) {
                return schedule;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String s = "";
        s = s + email;
        if (schedules.isEmpty()) {
            s = s + "\n[none]";
        }
        else {
            for (Schedule schedule : schedules) {
                s = s + "\n" + schedule;
            }
        }
        return s;
    }

}