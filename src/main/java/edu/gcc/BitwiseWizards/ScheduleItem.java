package edu.gcc.BitwiseWizards;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

public class ScheduleItem {
    private String name;
    private List<Character> days;
    private Date start;
    private Date end;

    ScheduleItem(String name, List<Character> meetingDays, Date start, Date end)
    {
        this.name = name;
        this.days = meetingDays;
        this.start = start;
        this.end = end;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addDays(Set<Character> days) {
        this.days.addAll(days);
    }

    public void removeDays(Set<Character> days) {
        this.days.removeAll(days);
    }

    public Set<Character> getDays() {
        return new HashSet<>(days);
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStart() {
        return start;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getEnd() {
        return end;
    }

    public boolean equals(ScheduleItem item) {
        return this.name.equals(item.getName());
    }

    public boolean conflicts(ScheduleItem item) {
        // Logic to determine conflicts between schedule items
        return false;
    }
}