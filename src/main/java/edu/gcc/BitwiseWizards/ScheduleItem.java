package edu.gcc.BitwiseWizards;

import java.util.*;

public class ScheduleItem {

    private int id;
    private String name;
    private Map<Character, List<Integer>> meetingTimes; // {"M" : [1100, 1150], "R" : [1400, 1515]}

    public ScheduleItem(int id, String name, Map<Character, List<Integer>> meetingTimes) {
        this.id = id;
        this.name = name;
        this.meetingTimes = meetingTimes;
    }

    public ScheduleItem(String name, Map<Character, List<Integer>> meetingTimes) {
        this.name = name;
        this.meetingTimes = meetingTimes;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMeetingTimes(Map<Character, List<Integer>> meetingTimes) {
        this.meetingTimes = meetingTimes;
    }

    public Map<Character, List<Integer>> getMeetingTimes() {
        return meetingTimes;
    }

    public Set<Character> getMeetingDays() { // useful for filtering by day?
        return meetingTimes.keySet();
    }

    //
    public boolean conflicts(ScheduleItem item) {
        // logic to determine time conflicts between schedule items
        for(Character day : meetingTimes.keySet()) {
            if(item.getMeetingDays().contains(day)) {
                int itemStart = item.getMeetingTimes().get(day).get(0);
                int itemEnd = item.getMeetingTimes().get(day).get(1);
                int currStart = meetingTimes.get(day).get(0);
                int currEnd = meetingTimes.get(day).get(1);
                if(itemStart <= currEnd && currStart <= itemEnd)
                    return true;
            }
        }
        return false;
    }
//
    // do we need this?
    public boolean equals(ScheduleItem item) {
        return this.name.equals(item.getName());
    }

    @Override
    public String toString() {
        // e.g. "Chapel" / "Software Engineering"
        return getName();
    }

}