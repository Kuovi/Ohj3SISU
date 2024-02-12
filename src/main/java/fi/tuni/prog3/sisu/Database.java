/*
 *
 */
package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;


public class Database {

    private static HashMap<String, DegreeProgramme> degreeProgrammeMap;

    private static HashMap<String, StudyModule> studyModuleMap;

    private static HashMap<String, GroupingModule> groupingModuleMap;

    private static HashMap<String, CourseUnit> courseUnitMap;
    
    private static Student student;

    //Should only be used in initializing the program
    private final static ArrayList<JSONObject> degreeProgrammes = new ArrayList<>();
    
    public Database() {

        degreeProgrammeMap = new HashMap<>();
        studyModuleMap = new HashMap<>();
        groupingModuleMap = new HashMap<>();
        courseUnitMap = new HashMap<>();

    }

    public static ArrayList<JSONObject> getDegreeProgrammes() {
        ArrayList<JSONObject> list = degreeProgrammes;
        return list;
    }

    public static void addDegreeProgrammesToList(JSONObject json) {

        JSONArray infos = json.getJSONArray("searchResults");
        for (int j = 0; j < infos.length(); j++) {
            degreeProgrammes.add(infos.getJSONObject(j));
        }
    }

    public void addDegreeProgramme(DegreeProgramme module) {
        degreeProgrammeMap.put(module.getGroupId(), module);
    }

    public void addStudyModule(StudyModule module) {
        studyModuleMap.put(module.getGroupId(), module);
    }

    public void addGroupingModule(GroupingModule module) {
        
        groupingModuleMap.put(module.getGroupId(), module);
    }

    public void addCourseUnit(CourseUnit course) {
        courseUnitMap.put(course.getGroupId(), course);
    }

    public static HashMap<String, CourseUnit> getCourseMap() {
        return courseUnitMap;
    }
    public static HashMap<String, DegreeProgramme> getDegreeProgrammeMap() {
        return degreeProgrammeMap;
    }
    public static HashMap<String, StudyModule> getStudyModuleMap() {
        return studyModuleMap;
    }
    public static HashMap<String, GroupingModule> getGroupingModuleMap() {
        return groupingModuleMap;
    }

    public static Student getStudent() {
        return student;
    }

    public static void setStudent(Student student) {
        Database.student = student;
    }    
    
}
