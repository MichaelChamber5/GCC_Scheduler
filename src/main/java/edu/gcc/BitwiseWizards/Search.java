package edu.gcc.BitwiseWizards;

import javax.xml.crypto.Data;
import java.util.*;

public class Search {
    private ArrayList<CourseItem> allCourses;
    private ArrayList<CourseItem> searchedCourses;
    private ArrayList<CourseItem> filteredCourses;
    private String deptCode = "";
    private List<Character> days = new ArrayList<>();
    private Date start = null;
    private Date end = null;
    private DatabaseManager dbm;

    public Search(DatabaseManager dbm) {
        this.dbm = dbm;
        searchedCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
        allCourses = new ArrayList<>();

        //initially populate the allCourses with all courses
        ArrayList<Integer> courseIDs = dbm.getAllCourseIds();
        for (int courseID : courseIDs) {
            allCourses.add(dbm.getCourseByID(courseID));
        }
    }

    //TODO: talk to Rhodes about how semester logic is handled
    public ArrayList<CourseItem> searchSingleWord(String keywordStr, String semester) {
        searchedCourses.clear();

        //SEARCHING FOR
        //name
        //meeting days
        //meeting times
        //location
        //course number
        //section
        //depCode
        //description
        //professors
        for(CourseItem c : allCourses) {
            System.out.println("Comparing " + c.getSemester() + " to " + semester);
            if(!c.getSemester().equals(semester)) //if we arent in the current semester then skip
            {
                continue;
            }
            if(keywordStr.isEmpty())//if we have the empty string
            {
                searchedCourses.add(c);
            }
            else if(keywordStr.length() == 1)//do character check
            {
                //check for character for section (A, B, C, D...) OR day (M, T, W...)
                if(Character.toString(c.getSection()).equalsIgnoreCase(keywordStr))
                {
                    searchedCourses.add(c);
                }
                else if(c.getDays().contains(keywordStr.charAt(0)))
                {
                    searchedCourses.add(c);
                }
            }
            else//check string
            {
                if(
                    c.getName().toLowerCase().contains(keywordStr.toLowerCase()) ||
                    //TODO: add dates and times
                    c.getLocation().toLowerCase().contains(keywordStr.toLowerCase()) ||
                    Integer.toString(c.getCourseNumber()).equalsIgnoreCase(keywordStr) ||
                    c.getDepCode().equalsIgnoreCase(keywordStr) ||
                    c.getDescription().toLowerCase().contains(keywordStr.toLowerCase()) ||
                    c.getProfessors().stream().anyMatch(p -> p.getName().toLowerCase().contains(keywordStr.toLowerCase()))
                )
                {
                    searchedCourses.add(c);
                }
            }
        }

        return searchedCourses;
    }

    public ArrayList<CourseItem> searchMultiWord(String[] words, String semester)
    {
        if (words == null || words.length == 0) {
            return new ArrayList<>();
        }

        ArrayList<CourseItem> result = new ArrayList<>(allCourses);

        // For each keyword, filter the results to keep only courses
        // that match that keyword
        for (String keyword : words) {
            if (keyword.trim().isEmpty()) {
                continue;  // Skip empty keywords
            }

            ArrayList<CourseItem> matchingCourses = new ArrayList<>();

            // Use similar logic as in searchSingleWord but applied to current result set
            for (CourseItem c : result) {
                if(!c.getSemester().equals(semester)) //if we arent in the current semester then skip
                {
                    continue;
                }
                if (keyword.length() == 1) {
                    // Check for character for section (A, B, C, D...) OR day (M, T, W...)
                    if (Character.toString(c.getSection()).equalsIgnoreCase(keyword) ||
                            c.getDays().contains(keyword.charAt(0))) {
                        matchingCourses.add(c);
                    }
                } else {
                    // Check string
                    if (c.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            c.getLocation().toLowerCase().contains(keyword.toLowerCase()) ||
                            Integer.toString(c.getCourseNumber()).equalsIgnoreCase(keyword) ||
                            c.getDepCode().equalsIgnoreCase(keyword) ||
                            c.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                            c.getProfessors().stream().anyMatch(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))) {
                        matchingCourses.add(c);
                    }
                }
            }

            // Update result to only include courses that matched this keyword
            result = matchingCourses;
        }

        return result;
    }

    /**
     * wrapper method for both single and multiword searches
     * if the keyword is "", returns all course
     * if the keyword is a single word, returns all courses that match that word
     * if the keyword is a sentence, returns all course that match all words in the sentence
     * @param keywordStr
     * @return list of courses that match the keyword(s)
     */
    public ArrayList<CourseItem> search(String keywordStr, String semester)
    {
        String[] keyWords = keywordStr.split(" ");
        if(keyWords.length <= 1)
        {
            return searchSingleWord(keywordStr, semester);
        }
        else //TODO: implement multi-word search
        {
            return searchMultiWord(keyWords, semester);
        }
    }

    //NOT CURRENTLY BEING USED
//    private List<CourseItem> performFuzzySearch(String keywordStr, User currUser, DatabaseManager dbm) {
//        ArrayList<Integer> allCourseIDs = dbm.searchCoursesFuzzy(keywordStr);
//        List<CourseItem> bestMatches = new ArrayList<>();
//
//        for (int courseID : allCourseIDs) {
//            bestMatches.add(dbm.getCourseByID(courseID));
//        }
//
//        return bestMatches;
//    }

    public List<CourseItem> filter(String deptCode, List<Character> days, Date start, Date end) {
        this.deptCode = deptCode;
        this.days = days;
        this.start = start;
        this.end = end;

        if (searchedCourses.isEmpty()) {
            return new ArrayList<>();
        }

        // Start filtering using only searchedCourses data
        filteredCourses = new ArrayList<>(searchedCourses);

        // Filter by department code
        if (deptCode != null && !deptCode.isEmpty()) {
            filteredCourses.removeIf(course -> !course.getDepCode().equalsIgnoreCase(deptCode));
        }

        // Filter by days
        if (days != null && !days.isEmpty()) {
            filteredCourses.removeIf(course -> !containsAnyDay(course.getDays(), days));
        }

        // Filter by start time
        if (start != null) {
            filteredCourses.removeIf(course -> {
                Integer courseStart = course.getStartTime();
                return courseStart != null && courseStart < convertDateToTime(start);
            });
        }

        // Filter by end time
        if (end != null) {
            filteredCourses.removeIf(course -> {
                Integer courseEnd = course.getEndTime();
                return courseEnd != null && courseEnd > convertDateToTime(end);
            });
        }

        return filteredCourses;
    }

    private boolean containsAnyDay(List<Character> courseDays, List<Character> requestedDays) {
        for (Character day : requestedDays) {
            if (courseDays.contains(day)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a `Date` object to an integer representing military time.
     * Example: 9:30 AM -> 930, 3:15 PM -> 1515
     */
    private int convertDateToTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return hour * 100 + minute;
    }

}

