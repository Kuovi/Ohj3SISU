/*
 * Luokka kuvaa yhtä kurssia.
 */
package fi.tuni.prog3.sisu;

public class CourseUnit {
    
    private String name;
    private String groupId;
    private String credits;
    private String coursecode;
    private String coursecontent;
    private String outcomes;

    public String getCoursecontent() {
        return coursecontent;
    }
    
    public String getCoursecode() {
        return coursecode;
    }
            
    public String getCredits() {
        return this.credits;
    }
    
    public String getGroupId(){
        return this.groupId;
    }

    public String getName() {
        return name;
    }

    public String getOutcomes() {
        return outcomes;
    }

    
    public CourseUnit(String groupId, String credits, String coursecode, String coursecontent, String coursename, String outcomes) {
        this.groupId = groupId;
        this.credits = credits;
        this.coursecode = coursecode;
        this.coursecontent = coursecontent;
        this.name = coursename;
        this.outcomes = outcomes;
    }


}
