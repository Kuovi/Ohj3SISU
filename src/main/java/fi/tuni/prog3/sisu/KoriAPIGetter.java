/**
 * Imports the university database of courses and study structures from the 
 * public Sisu Kori API, which docs are at:
 * https://sis-tuni.funidata.fi/kori/swagger-ui/index.html?configUrl=/kori/v3/ap
 * i-docs/swagger-config&filter=true#/module-controller/findByGroupIdIntegrator
 *
 * Items are used the same way as in the database, with course groupings being 
 * subclasses of the abstract Module, and course units are a class of their own.
 * Each grouping knows which courses and/or submodules it contains.
 */
package fi.tuni.prog3.sisu;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KoriAPIGetter {
    
    private static final Database database = new Database();
    
    private static JSONObject getUrlAsArray(String url) throws IOException { 
        JSONArray jArray = new JSONArray(); 
        JSONObject json = new JSONObject();
        //StringBuilder b = new StringBuilder();
        
        jArray = new JSONArray(IOUtils.toString(new URL(url), "UTF-8"));
        for(int i=0;i<jArray.length();i++){
            json = jArray.getJSONObject(i);
        }
        if (json.isEmpty()) {
                System.out.print(url + " is null! \n");
        }
        return json;  
    }  
    
    /**
     * Function returns a JSON from the specified url.
     */
    public static JSONObject getJSONFromAPI(String url) {
        
        JSONObject json = new JSONObject();
        try {
            // These are JSONArrays for some reason.
            if (url.contains("by-group-id") ) {
                json = getUrlAsArray(url);
            }
            else {
                json = new JSONObject(IOUtils.toString(new URL(url), "UTF-8"));
                if (json.isEmpty()) {
                    System.out.print(url + " is null! \n");
                return json;
        }
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(Sisu.class.getName())
                    .log(Level.SEVERE, null, ex);
        } 
        catch (JSONException ex) {
            Logger.getLogger(Sisu.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return json;
    }
    
    public static TreeMap<String,ArrayList<String>> readRule(JSONObject rule,
                                       TreeMap<String,ArrayList<String>> cMap) {
        
        if ("CreditsRule".equals(rule.getString("type")) || 
                         rule.get("type") == "CourseUnitCountRule") {
            JSONObject subRule = rule.getJSONObject("rule");
            cMap = readRule(subRule, cMap);
        }
        // Go through all rules under CompositeRule.
        else if ("CompositeRule".equals(rule.getString("type"))) {
            JSONArray rules = rule.getJSONArray("rules");
            for(int n = 0; n < rules.length(); n++) {
                JSONObject r = rules.getJSONObject(n);
                cMap = readRule(r, cMap);
            }
        }
        // Add course rule to the map.
        else if ("CourseUnitRule".equals(rule.getString("type"))) {
            String courseId = rule.getString("courseUnitGroupId");
            ArrayList<String> courses = cMap.get("courseGroupId");
            courses.add(courseId);
            cMap.replace("courseGroupId", courses);
        }
        // Same for module.
        else if ("ModuleRule".equals(rule.getString("type"))){
            String moduleId = rule.getString("moduleGroupId");
            ArrayList<String> modules = cMap.get("moduleGroupId");
            modules.add(moduleId);
            cMap.replace("moduleGroupId", modules);
            
        }
        // Known to exist but require no actions as of now.
        else if ("AnyCourseUnitRule".equals(rule.getString("type"))){
        }
        else if ("AnyModuleRule".equals(rule.getString("type"))){
        }
        // Should never happen.
        else { 
            var str = rule.get("type");

            System.out.print("Unsupported rule = " + str + "\n");     

        }
        return cMap;
    }
    
    public static void 
        initClass(JSONObject json, TreeMap<String,ArrayList<String>> childMap) {
        String name;
        if(json.getJSONObject("name").has("fi")){
            name = json.getJSONObject("name").get("fi").toString();
        }
        else{
            name = json.getJSONObject("name").get("en").toString();
        }   
        if (json.has("type")) {
         
            if (null != json.getString("type")) switch (json.getString("type")){
                case "DegreeProgramme":{
                    
                    var module = new DegreeProgramme(
                            name,
                            json.getString("id"),
                            json.getString("groupId"),
                            json.getJSONObject("targetCredits")
                                                         .get("min").toString(),
                            json.get("code").toString(),
                            childMap);
                    database.addDegreeProgramme(module);
                        break;
                    }
                case "StudyModule":{
                    var module = new StudyModule(
                            name,
                            json.get("groupId").toString(),
                            json.getJSONObject("targetCredits").getInt("min"),
                            json.get("contentDescription").toString(),
                            json.get("studyLevel").toString(),
                            json.get("code").toString(),
                            childMap);
                    database.addStudyModule(module);
                        break;
                    }
                case "GroupingModule":{

                    var module = new GroupingModule(name, 
                             json.get("groupId").toString(),json.get("code").toString(), childMap);
                    database.addGroupingModule(module);
                        break;
                    }
                default:
                    break;
            }
        }
        // Courseunits dont have type
        else {
            var course = new CourseUnit(json.getString("groupId"),
                    json.getJSONObject("credits").toString(),
                    json.getString("code"), 
                    json.get("content").toString(),
                    name, 
                    json.get("outcomes").toString());
                database.addCourseUnit(course);
        }
        
    }
      
    /**
     * Goes through a module JSONObject and creates a Module class out of it.
     * Also creates submodules and subclasses recursively.
     */
    public static void readModuleData(JSONObject module, String id) {
        
        String moduleGroupIdUrl = "https://sis-tuni.funidata.fi/kori/api/"
                                               + "modules/by-group-id?groupId=";
        String courseGroupUrl = "https://sis-tuni.funidata.fi/kori/api/"
                                          + "course-units/by-group-id?groupId=";
        String uniRootUrl = "&universityId=tuni-university-root-id";
        
            
        // Contains all groupIds and courseIds directly under this module.
        TreeMap<String,ArrayList<String>> childMap = new TreeMap<>();
        childMap.put("courseGroupId", new ArrayList<String>());
        childMap.put("moduleGroupId", new ArrayList<String>());
        if (id == null) {
            id = module.getString("groupId");
        }
        // Metadata of the module.
        JSONObject json = getJSONFromAPI(moduleGroupIdUrl+id+uniRootUrl);
        var programRule = json.getJSONObject("rule");
        // Rules of this module.
        childMap = readRule(programRule, childMap);
        // Initialize module class.
        initClass(json, childMap);
        
        // Read and init all course instances found.
        for (var course : childMap.get("courseGroupId")) {
            JSONObject courseJson = 
                               getJSONFromAPI(courseGroupUrl+course+uniRootUrl);
            initClass(courseJson, null);
        }
        // And submodules.
        for (var groupId : childMap.get("moduleGroupId")) {
            readModuleData(null, groupId);
        }
    }
}
