/**
 * Opintokokonaisuus, kuvaa tutkintorakenteita.
 */
package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import java.util.TreeMap;


public class DegreeProgramme extends Module {

    private final String targetCredits;

    public String getTargetCredits() {
        return targetCredits;
    }
 
    public DegreeProgramme(String name,String id, String groupId, String targetCredits, String code, TreeMap<String,ArrayList<String>> children) {
        super.name = name;
        super.id = id;
        super.groupId = groupId;
        this.targetCredits = targetCredits;
        super.code = code;
        super.children = children;
        
    }
    
    
}
