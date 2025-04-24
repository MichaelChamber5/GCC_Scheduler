package edu.gcc.BitwiseWizards;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

public class Search {
    private SpellChecker spellChecker;

    private ArrayList<CourseItem> allCourses;
    private ArrayList<CourseItem> searchedCourses;
    private ArrayList<CourseItem> filteredCourses;
    private String deptCode = "";
    private List<Character> days = new ArrayList<>();
    private Date start = null;
    private Date end = null;
    private DatabaseManager dbm;

    public Search(DatabaseManager dbm) throws IOException {
        this.dbm = dbm;
        searchedCourses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
        allCourses = new ArrayList<>();

        //initially populate the allCourses with all courses
        ArrayList<Integer> courseIDs = dbm.getAllCourseIds();

        System.out.println("Size of course IDs: " + courseIDs.size());

        for (int courseID : courseIDs) {
            allCourses.add(dbm.getCourseByID(courseID));
        }

        System.out.println("Loaded " + allCourses.size() + " courses from the database.");

        try {
            // Load dictionary from resources
            InputStream dictionaryStream = getClass().getClassLoader().getResourceAsStream("dictionary/wordsActually.txt");
            if (dictionaryStream == null) {
                throw new IOException("Dictionary file not found in resources");
            }

            // Create a temporary file to work with Jazzy API which expects a File object
            File tempDictFile = File.createTempFile("dictionary", ".txt");
            tempDictFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempDictFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = dictionaryStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            SpellDictionary dictionary = new SpellDictionaryHashMap(tempDictFile);
            spellChecker = new SpellChecker(dictionary);
        }
        catch (Exception e) {
            System.out.println("Error loading spell check dictionary: " + e.getMessage());
            e.printStackTrace();
            spellChecker = null;
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
            if(!semester.isEmpty() && !c.getSemester().equals(semester)) //if we arent in the current semester then skip
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
                if(!semester.equals("") && !c.getSemester().equals(semester)) //if we arent in the current semester then skip
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

        searchedCourses = new ArrayList<>(result);
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
    public ArrayList<CourseItem> search(String keywordStr, String semester) {
        String[] keyWords = keywordStr.split(" ");
        ArrayList<CourseItem> resultingItems = new ArrayList<>();

        try {
            if (keyWords.length > 2 || keywordStr.length() > 20) { // trigger LLM for long queries
                GeminiKeywordExtractor extractor = new GeminiKeywordExtractor("YOUR_API_KEY");
                List<String> extractedKeywords = extractor.extractKeywords(keywordStr);
                System.out.println("LLM extracted keywords: " + extractedKeywords);
                resultingItems = searchMultiWord(extractedKeywords.toArray(new String[0]), semester);
            } else {
                resultingItems = (keyWords.length == 1)
                        ? searchSingleWord(keyWords[0], semester)
                        : searchMultiWord(keyWords, semester);
            }
        } catch (Exception e) {
            System.err.println("LLM keyword extraction failed, falling back to standard search.");
            resultingItems = (keyWords.length == 1)
                    ? searchSingleWord(keyWords[0], semester)
                    : searchMultiWord(keyWords, semester);
        }

        // If empty, try spell check fallback
        if (resultingItems.isEmpty()) {
            for (int i = 0; i < keyWords.length; i++) {
                keyWords[i] = keyWords[i].trim();
                if (spellChecker != null && !keyWords[i].isEmpty() && !spellChecker.isCorrect(keyWords[i])) {
                    String suggestion = getBestSuggestion(keyWords[i]);
                    if (suggestion != null && !suggestion.isEmpty()) {
                        keyWords[i] = suggestion;
                    }
                }
            }

            resultingItems = (keyWords.length == 1)
                    ? searchSingleWord(keyWords[0], semester)
                    : searchMultiWord(keyWords, semester);
        }

        return resultingItems;
    }


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

    // Get suggestions for a misspelled word
    public List<String> getSuggestions(String word, int suggestionCount) {
        if (spellChecker == null || word == null || word.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<String> suggestions = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<?> rawSuggestions = spellChecker.getSuggestions(word, suggestionCount);

            if (rawSuggestions != null) {
                for (Object suggestion : rawSuggestions) {
                    // Check if it's a Word object and extract the text
                    if (suggestion instanceof com.swabunga.spell.engine.Word) {
                        suggestions.add(suggestion.toString());
                    }
                    // If it's already a String
                    else if (suggestion instanceof String) {
                        suggestions.add((String) suggestion);
                    }
                }
            }

            System.out.println("Suggestions for " + word + ": " + suggestions);
            return suggestions.subList(0, Math.min(suggestions.size(), suggestionCount));
        } catch (Exception e) {
            System.err.println("Error getting spell suggestions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get the best suggestion for a misspelled word
    public String getBestSuggestion(String word) {
        if (spellChecker == null || word == null || word.isEmpty()) {
            return word;
        }

        try {
            List<String> suggestions = getSuggestions(word, 1);
            if(!suggestions.isEmpty())
                System.out.println("Here's the word I think you were trying to spell: " + suggestions.get(0));
            return suggestions.isEmpty() ? word : suggestions.get(0);
        } catch (Exception e) {
            System.err.println("Error getting best suggestion: " + e.getMessage());
            e.printStackTrace();
            return word;
        }
    }
}