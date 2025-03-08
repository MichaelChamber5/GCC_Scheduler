package edu.gcc.BitwiseWizards;

public class User {

    private String email;
    private String pwd_hash;
    private Schedule schedule;

    public User(String email, String password) {
        setEmail(email);
        setPwd(password);
        schedule = new Schedule();
    }

    public void setEmail(String email) {
        // TODO: check if valid email
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPwd(String pwd) {
        // TODO: implement password hashing
        this.pwd_hash = pwd;
    }

    public void addScheduleItem() {

    }

    public Schedule getSchedule() {
        return schedule;
    }

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