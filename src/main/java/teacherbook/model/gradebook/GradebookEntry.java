package teacherbook.model.gradebook;

import teacherbook.model.users.Student;

import javax.persistence.*;
import java.util.Comparator;

@Entity
public class GradebookEntry implements Comparable<GradebookEntry>{

    @Id
    @Column(name="gradebook_entry_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="gradebook_id")
    private Gradebook gradebook;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name="assignment_id")
    private Assignment assignment;

    private int grade;

    private String note;

    private boolean attendance;

    private boolean attendanceValue;

    public GradebookEntry() {
        super();
    }

    public GradebookEntry(Gradebook g, Student s, Assignment a) {
        this.gradebook = g;
        this.student = s;
        this.assignment = a;
    }

    public Long getId() {
        return id;
    }

    public Gradebook getGradebook() {
        return gradebook;
    }

    public Student getStudent() {
        return student;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public int getGrade() {
        return grade;
    }

    public String getNote() {
        return note;
    }

    public boolean isAttendance() {
        return attendance;
    }

    public boolean isAttendanceValue() {
        return attendanceValue;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAttendance(boolean attendance) {
        this.attendance = attendance;
    }

    public void setAttendanceValue(boolean attendanceValue) {
        this.attendanceValue = attendanceValue;
    }

    @Override
    public int compareTo(GradebookEntry o) {
        return this.student.compareTo(o.getStudent());
    }
}
