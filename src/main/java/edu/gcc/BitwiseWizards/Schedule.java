package edu.gcc.BitwiseWizards;

import java.util.*;
public class Schedule {
    private List<ScheduleItem> items = new ArrayList<>();
    private int credit_count = 0;

    public List<ScheduleItem> getScheduleItems() {
        return items;
    }

    public List<CourseItem> getCourses() {
        return new ArrayList<>(); // Placeholder
    }

    public int getCreditCount() {
        return credit_count;
    }

    public void addScheduleItem(ScheduleItem item) {
        // Implementation here
    }

    public void removeScheduleItem(ScheduleItem item) {
        // Implementation here
    }
}