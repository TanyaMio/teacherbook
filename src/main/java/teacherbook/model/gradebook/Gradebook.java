package teacherbook.model.gradebook;

import teacherbook.model.Course;
import teacherbook.model.StudentGroup;
import teacherbook.model.schedulegrid.Semester;
import teacherbook.model.users.Teacher;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class Gradebook {

    @Id
    @Column(name="gradebook_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @ManyToOne
    @JoinColumn(name="semester_id")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name="group_id")
    private StudentGroup group;

    @ManyToOne
    @JoinColumn(name="course_id")
    private Course course;

    @ManyToMany(mappedBy = "gradebooks")
    private Collection<Teacher> teachers;

    @OneToMany(mappedBy = "gradebook")
    private Collection<Assignment> assignments;

    @OneToMany(mappedBy = "gradebook")
    private Collection<GradebookEntry> entries;

    public Gradebook() {
        super();
    }

    public Gradebook(String name, Semester sem, StudentGroup group, Course course) {
        this.name = name;
        this.semester = sem;
        this.group = group;
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Semester getSemester() {
        return semester;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public Course getCourse() {
        return course;
    }

    public Collection<Teacher> getTeachers() {
        return teachers;
    }

    public Collection<Assignment> getAssignments() {
        return assignments;
    }

    public Collection<GradebookEntry> getEntries() {
        return entries;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Gradebook && ((Gradebook) o).getId().equals(this.id);
    }

}
