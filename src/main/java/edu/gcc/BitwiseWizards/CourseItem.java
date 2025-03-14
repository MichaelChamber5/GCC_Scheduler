package edu.gcc.BitwiseWizards;

import java.util.Date;
import java.util.List;

public class CourseItem extends ScheduleItem {
    private String depCode;
    private int courseCode;
    private char section;
    private String location;
    private String description;
    private Professor professor;
    private int credits;
    private boolean isLab;
    private boolean onSchedule = false;

    public CourseItem(String name, List<Character> meetingDays, Date start, Date end, String depCode,
                      int courseCode, char section, String location, String description,
                      Professor professor, int credits, boolean onSchedule, boolean isLab)
    {
        super(name, meetingDays, start, end);
        this.depCode = depCode;
        this.courseCode = courseCode;
        this.section = section;
        this.location = location;
        this.description = description;
        this.professor = professor;
        this.credits = credits;
        this.onSchedule = onSchedule;
        this.isLab = isLab;
    }

    public String getDepCode() {
        return depCode;
    }

    public void setDepCode(String depCode) {
        this.depCode = depCode;
    }

    public int getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(int courseCode) {
        this.courseCode = courseCode;
    }

    public char getSection() {
        return section;
    }

    public void setSection(char section) {
        this.section = section;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public boolean equals(CourseItem item) {
        return this.courseCode == item.courseCode && this.section == item.section;
    }
}