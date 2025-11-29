package studentmanagement;

/**
 * Represents a student in the system.
 * <p>
 * Each student has a name, a unique student number, a field of study and a GPA.
 * This class encapsulates this information and provides simple getters and setters
 * along with equality and hash code implementations. Having a dedicated class
 * for student data encapsulation makes it easy to extend the model later
 * (e.g. adding email address, year of entry, etc.).
 */
public class Student {
    private String name;
    private String studentNumber;
    private String fieldOfStudy;
    private double gpa;

    /**
     * Constructs a new student.
     *
     * @param name          the student's full name
     * @param studentNumber unique identifier for the student
     * @param fieldOfStudy  the student's major or field of study
     * @param gpa           the student's grade point average
     */
    public Student(String name, String studentNumber, String fieldOfStudy, double gpa) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.fieldOfStudy = fieldOfStudy;
        this.gpa = gpa;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Double.compare(student.gpa, gpa) == 0 &&
                name.equals(student.name) &&
                studentNumber.equals(student.studentNumber) &&
                fieldOfStudy.equals(student.fieldOfStudy);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        result = 31 * result + studentNumber.hashCode();
        result = 31 * result + fieldOfStudy.hashCode();
        temp = Double.doubleToLongBits(gpa);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", studentNumber='" + studentNumber + '\'' +
                ", fieldOfStudy='" + fieldOfStudy + '\'' +
                ", gpa=" + gpa +
                '}';
    }
}