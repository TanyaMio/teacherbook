package teacherbook.model;

import teacherbook.model.users.School;
import teacherbook.model.users.Student;

import javax.persistence.*;
import java.util.Collection;

@Entity
public class StudentGroup {

    @Id
    @Column(name="group_id")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name="school_id")
    private School school;

    @ManyToMany(mappedBy = "groups")
    private Collection<Student> students;

    public StudentGroup() {
        super();
    }

    public StudentGroup(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Collection<Student> getStudents() {
        return students;
    }

    public void setStudents(Collection<Student> students) {
        this.students = students;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof StudentGroup) && this.id.equals(((StudentGroup) o).getId());
    }
}
