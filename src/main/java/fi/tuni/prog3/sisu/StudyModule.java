/**
 * Module which contains courses and submodules.
 */
package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import java.util.TreeMap;


public class StudyModule extends Module {
    
    private final int targetCredits;
    private final String contentDescription;
    private final String studyLevel;

    
    public int getTargetCredits() {
        return targetCredits;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public String getStudyLevel() {
        return studyLevel;
    }

    public StudyModule(String name, String groupId, int targetCredits, String contentDescription, String studyLevel,
            String code, TreeMap<String,ArrayList<String>> children) {
        super.name = name;
        super.groupId = groupId;
        this.targetCredits = targetCredits;
        this.contentDescription = contentDescription;
        this.studyLevel = studyLevel;
        super.code = code;
        super.children = children;
    }
    
   
}