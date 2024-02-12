/**
 * Class portrays the attributes of a student using the application.
 */

package fi.tuni.prog3.sisu;
import java.util.ArrayList;


public class Student {
    
    private final String name;
    private final String student_number;
    private int total_credits = 0;

    //List of done courses
    private ArrayList<String> completedcourses = new ArrayList<>();

    public Student(String name, String student_number) {
        this.name = name;
        this.student_number = student_number;
    }
    
    public String getName() {
        return name;
    }

    public String getStudent_number() {
        return student_number;
    }

    public ArrayList<String> getCompletedcourses() {
        return completedcourses;
    }

    public void setTotal_credits(int total_credits) {
        this.total_credits = total_credits;
    }


    public int getTotal_credits() {
        return total_credits;
    }
    
    // Mark course as completed.
    public void courseDone(String courseId){
        completedcourses.add(courseId);
        
        String credits = Database.getCourseMap().get(courseId).getCredits();
        int cred = Integer.parseInt(credits.substring(7,8));
        total_credits += cred;
    }
}
