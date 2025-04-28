package edu.gcc.BitwiseWizards;

import java.util.*;

public class Schedule {

    private int sched_id;
    private String name;
    private int credit_count;
    private List<ScheduleItem> items;

    public Schedule(int sched_id, String name) {
        this.sched_id = sched_id;
        this.name = name;
        credit_count = 0;
        items = new ArrayList<>();
    }

    public int getID() {
        return sched_id;
    }

    public String getName() {
        return name;
    }

    public int getCreditCount() {
        return credit_count;
    }

    public void setScheduleItems(List<ScheduleItem> items) {
        this.items = items;
        this.credit_count = 0;
        for (ScheduleItem item : items) {
            if (item instanceof CourseItem) {
                credit_count += ((CourseItem) item).getCredits();
            }
        }
    }

    /**
     * @return list of all items currently on the user's schedule
     */
    public List<ScheduleItem> getScheduleItems() {
        return items;
    }

    /**
     * @return list of all courses currently on the user's schedule
     */
    public List<CourseItem> getCourses() {
        ArrayList<CourseItem> courses = new ArrayList<>();
        for (ScheduleItem item : items) {
            if (item instanceof CourseItem) {
                courses.add((CourseItem) item);
            }
        }
        return courses;
    }

    /**
     * Add item to schedule.
     * @param item
     * @return
     */
    public boolean addScheduleItem(ScheduleItem item) {
        if(safeToAdd(item))
        {
            //if the item is a course item, add to credit count
            if(item instanceof CourseItem)
            {
                credit_count += ((CourseItem) item).getCredits();
            }
            items.add(item);
            return true;
        }
            //TODO: Add pop-up to warn user!
            System.out.println("ERROR: overlapping schedule items");
            return false;
    }

    /**
     * Remove item from schedule.
     * @param item
     */
    public void removeScheduleItem(ScheduleItem item) {
        //if the item is a course item, reduce to credit count
        if(item instanceof CourseItem)
        {
            credit_count -= ((CourseItem) item).getCredits();
        }
        items.remove(item);
    }

    public boolean safeToAdd(ScheduleItem otherItem)
    {
        for(ScheduleItem item : items)
        {
            if(otherItem.conflicts(item))
            {
                return false;
            }
        }
        return true;
    }

    private int convertDateToTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return hour * 100 + minute;
    }

    @Override
    public String toString() {
        // [Software Engineering, Chapel, ...]
        return name + ": " + items;
    }

    public boolean equals(Schedule other) {
        return this.sched_id == other.sched_id;
    }

}