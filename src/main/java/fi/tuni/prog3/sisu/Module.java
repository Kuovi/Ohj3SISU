/**
 * Abstract superclass representing a module.
 * Closely copies the API's Module. Supertype of DegreeProgramme, 
 * StudyModule, and GroupingModule.
 */
package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import java.util.TreeMap;


public abstract class Module {
    
    /**
     *  Unique id for the module. 
     */
    public String id;
    
    /**
     * Group id for the module. This is used to import modules.  
     */
    public String groupId;
    
    /**
     * Name of the module
     */
    public String name;
    
    
    /**
     * Code shown in the user interface.
     */
    public String code;
    
    /**
     * Type of the module. Might be redundant but not sure yet.
     * Can be either Education, DegreeProgramme, StudyModule, or GroupingModule.
     */
    public String type;
    
    /**
     * Contains all module and course groupIds that are directly under this 
     * module.
     */
    public TreeMap<String,ArrayList<String>> children;
    
    /**
     * Get module id
     */
    public String getId(){
        return this.id;
    }
    
    public String getGroupId(){
        return this.groupId;
    }
    
    /**
     * Get module name
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Return which courses/submodules are under this module.
     */
    public TreeMap<String,ArrayList<String>> getChildren() {
        
        return this.children;
    }
    
    
}
