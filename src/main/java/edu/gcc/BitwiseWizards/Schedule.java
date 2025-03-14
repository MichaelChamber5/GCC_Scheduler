package edu.gcc.BitwiseWizards;

import java.util.*;

public class Schedule {

    private List<ScheduleItem> items;
    private int credit_count;

    public Schedule() {
        items = new ArrayList<>();
        credit_count = 0;
    }

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
        //if the item is a course item, add to credit count
        if(item instanceof CourseItem)
        {
            credit_count += ((CourseItem) item).getCredits();
        }
        items.add(item);
    }

    public void removeScheduleItem(ScheduleItem item) {
        //if the item is a course item, reduce to credit count
        if(item instanceof CourseItem)
        {
            credit_count -= ((CourseItem) item).getCredits();
        }
        items.remove(item);
    }
}