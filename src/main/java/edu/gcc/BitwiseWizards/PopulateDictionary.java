package edu.gcc.BitwiseWizards;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class PopulateDictionary {
    public static void main(String[] args) {
        //ONLY RUN THIS WHEN YOU WANT TO POPULATE THE DICTIONARY
        //the dictionary will be stored under the path "dictionary/words.txt"

        ArrayList<CourseItem> courseItems = new ArrayList<>();
        populateCourseItemList(courseItems);

        System.out.println("Example output for single course: " + courseItems.get(0).getStringsForDictionary());

        ArrayList<String> courseItemStrings = createStringList(courseItems);
        createFile(courseItemStrings);

        //things that will be added to the dictionary:
        //name
        //location
        //dep code
        //description
        //professors
    }

    public static void populateCourseItemList(ArrayList<CourseItem> courseItemList)
    {
        NewDatabaseManager dbm = new NewDatabaseManager();
        //initially populate the allCourses with all courses
        ArrayList<Integer> courseIDs = dbm.getAllCourseIds();

        for (int courseID : courseIDs) {
            courseItemList.add(dbm.getCourseByID(courseID));
        }
    }

    public static void createFile(ArrayList<String> s)
    {
        try
        {
            File file = new File("src/main/resources/Dictionary/wordsActually.txt");
            PrintWriter pw = new PrintWriter(file);

            for (String str : s)
            {
                pw.println(str);
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e)
        {
            System.err.println("Failed to generate file and write to it...");
            e.printStackTrace();
        }
    }

    public static ArrayList<String> createStringList(ArrayList<CourseItem> items)
    {
        ArrayList<String> strings = new ArrayList<>();
        for (CourseItem item : items)
        {
            for (String str : item.getStringsForDictionary())
            {
                if(!strings.contains(str))
                {
                    strings.add(str);
                }
            }
        }
        return strings;
    }
}
