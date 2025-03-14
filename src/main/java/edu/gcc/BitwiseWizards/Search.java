package edu.gcc.BitwiseWizards;

import java.util.*;
public class Search {
    private List<CourseItem> searchedCourses;
    private List<CourseItem> filteredCourses;
    private String keywordStr = "";
    private String deptCode = "";
    private List<Character> days = new ArrayList<>();
    private Date start = null;
    private Date end = null;

    public List<CourseItem> search(String keywordStr, User currUser) {
        //for all course items
        //if course item contains keyword
            //add it to list
        //return final list
        DatabaseManager dbm = new DatabaseManager();
        ArrayList<Integer> courseIDs = dbm.searchCoursesByKeyword(keywordStr);

    }

    public List<CourseItem> filter(String deptCode, List<Character> days, Date start, Date end) {
        return new ArrayList<>(); // Placeholder
    }
}