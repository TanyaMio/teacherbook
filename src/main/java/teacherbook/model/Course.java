package teacherbook.model;

import teacherbook.model.users.School;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Course {

    @Id
    @Column(name="course_id")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @NotNull
    private String name;

    private int credits;

    public Course() {
        super();
    }

    public Course(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public School getSchool() {
        return school;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public boolean equals (Object o) {
        return (o instanceof Course && ((Course) o).name.equals(this.name));
    }
}
