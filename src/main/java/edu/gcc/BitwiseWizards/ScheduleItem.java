package edu.gcc.BitwiseWizards;

import java.util.*;

/**
 * ScheduleItem:
 * Author: Team Bitwise Wizards -Aiden, Micheal, Hannah
 * The ScheduleItem class is responsible for creating and managing schedule items.
 * Schedule items can be courses or personal items.
 * Schedule items have a name and a map of meeting times.
 * Meeting times are stored as a map of days to a list of start and end times.
 * e.g. {"M" : [1100, 1150], "R" : [1400, 1515]}
 * Schedule items can be checked for conflicts with other schedule items.
 * Schedule items can be compared for equality.
 * Schedule items can be displayed as a string.
 */

/**
 * ScheduleItem:
 * Author: Team Bitwise Wizards -Aiden, Micheal, Hannah
 * The ScheduleItem class is responsible for creating and managing schedule items.
 */
public class ScheduleItem {

    private int id;
    private String name;
    private Map<Character, List<Integer>> meetingTimes; // {"M" : [1100, 1150], "R" : [1400, 1515]}
    // TODO: add user_id?

    public ScheduleItem(int id, String name, Map<Character, List<Integer>> meetingTimes) {
        this.id = id;
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

    /**
     * Check for time conflicts between schedule items.
     * @param item
     * @return
     */

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

    // Unused
    //created for testing purposes
    public boolean equals(ScheduleItem item) {
        return this.name.equals(item.getName());
    }

    /**
     * Display the schedule item as a string.
     * e.g. "Chapel" / "Software Engineering"
     * @return
     */
    @Override
    public String toString() {
        // e.g. "Chapel" / "Software Engineering"
        return getName();
    }

}