package edu.gcc.BitwiseWizards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CourseItem extends ScheduleItem {

    //private int id;
    private int credits;
    private boolean isLab;
    private String location;
    private int courseNumber;
    private char section;
    private String semester;
    private String depCode;
    private String description;
    private ArrayList<Professor> professors;
    private boolean onSchedule;

    public CourseItem(int id, int credits, boolean isLab, String location, String courseName, int courseNumber,
                      char section, String semester, String depCode, String description,
                      ArrayList<Professor> professors, Map<Character, List<Integer>> meetingTimes,
                      boolean onSchedule) {
        super(id, courseName, meetingTimes);
        this.credits = credits;
        this.isLab = isLab;
        this.location = location;
        this.courseNumber = courseNumber;
        this.section = section;
        this.semester = semester;
        this.depCode = depCode;
        this.description = description;
        this.professors = professors;
        this.onSchedule = onSchedule;
    }

    public int getCredits() {
        return credits;
    }

    public boolean getIsLab() {
        return isLab;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    public char getSection() {
        return section;
    }

    public void setSection(char section) {
        this.section = section;
    }

    public String getSemester() {
        return semester;
    }

    public String getDepCode() {
        return depCode;
    }

    public void setDepCode(String depCode) {
        this.depCode = depCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Professor> getProfessors() {
        return professors;
    }

    public void setProfessors(ArrayList<Professor> professors) {
        this.professors = professors;
    }

    public boolean getOnSchedule() {
        return onSchedule;
    }

    public void setOnSchedule(boolean onSchedule) {
        this.onSchedule = onSchedule;
    }

    public boolean equals(CourseItem item) {
        return this.depCode.equals(item.depCode) && this.courseNumber == item.courseNumber
                && this.section == item.section && this.semester.equals(item.semester);
    }

    /**
     * Returns a list of days the course meets.
     * @return List of characters representing days (e.g., ['M', 'W', 'F'])
     */
    public List<Character> getDays() {
        return new ArrayList<>(getMeetingTimes().keySet());
    }

    /**
     * Returns the earliest start time for this course.
     * @return Start time in military format (e.g., 930 for 9:30 AM)
     */
    public Integer getStartTime() {
        int earliest = Integer.MAX_VALUE;
        for (List<Integer> times : getMeetingTimes().values()) {
            if (!times.isEmpty() && times.get(0) < earliest) {
                earliest = times.get(0);
            }
        }
        return earliest == Integer.MAX_VALUE ? null : earliest;
    }

    /**
     * Returns the latest end time for this course.
     * @return End time in military format (e.g., 1500 for 3:00 PM)
     */
    public Integer getEndTime() {
        int latest = Integer.MIN_VALUE;
        for (List<Integer> times : getMeetingTimes().values()) {
            if (times.size() > 1 && times.get(1) > latest) {
                latest = times.get(1);
            }
        }
        return latest == Integer.MIN_VALUE ? null : latest;
    }

    @Override
    public String toString() {
        return getName() + " " + section + " (" + semester.charAt(5) + ")";
    }

    public String getCourseName() {
        return getName();
    }

    @Override
    public boolean conflicts(ScheduleItem other) {
        if (!(other instanceof CourseItem)) {
            return false;
        }
        CourseItem otherCourse = (CourseItem) other;
        // Only consider a conflict if both courses are in the same semester.
        if (!this.semester.equals(otherCourse.getSemester())) {
            return false;
        }

        // Check for overlapping meeting times on common days.
        for (Character day : this.getMeetingTimes().keySet()) {
            if (otherCourse.getMeetingTimes().containsKey(day)) {
                List<Integer> thisTimes = this.getMeetingTimes().get(day);
                List<Integer> otherTimes = otherCourse.getMeetingTimes().get(day);
                int thisStart = thisTimes.get(0);
                int thisEnd = thisTimes.get(1);
                int otherStart = otherTimes.get(0);
                int otherEnd = otherTimes.get(1);
                // Overlap exists if one course starts before the other ends and vice versa.
                if (thisStart < otherEnd && otherStart < thisEnd) {
                    return true;
                }
            }
        }
        return false;
    }
}
