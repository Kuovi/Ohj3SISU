/**
 * Opintokokonaisuus, kuin StudyModule mutta ei sisällä kaikkea tietoa.
 */
package fi.tuni.prog3.sisu;

import java.util.ArrayList;
import java.util.TreeMap;

public class GroupingModule extends Module {
   


    public GroupingModule( String name, String groupId,  String code,
                                   TreeMap<String,ArrayList<String>> children) {
        super.name = name;
        super.groupId = groupId;
        super.code = code;
        super.children = children;
    }

    
}
