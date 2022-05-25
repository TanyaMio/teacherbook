package teacherbook.model.users;

import teacherbook.model.StudentGroup;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long student_id;

    @OneToOne(mappedBy = "student")
    private TeacherbookUser user;

    @NotNull
    private String fullname;

    @NotNull
    private String first_name;

    @NotNull
    private String last_name;

    @ManyToOne
    @JoinColumn(name="school_id")
    private School school;

    @ManyToMany
    @JoinTable(
            name = "student_groups",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Collection<StudentGroup> groups;

    public String getFullName() {
        return this.first_name + " " + this.last_name;
    }

    public Student(String first_name, String last_name) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.fullname = this.getFullName();
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
        this.fullname = this.getFullName();
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
        this.fullname = this.getFullName();
    }

    public Student() {
        super();
    }

    public Long getStudent_id() {
        return student_id;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public String getFullname() {
        return fullname;
    }

    public Collection<StudentGroup> getGroups() {
        return groups;
    }

    public TeacherbookUser getUser() {
        return user;
    }

    public void addGroup(StudentGroup group) {
        if(!this.groups.contains(group)) {
            this.groups.add(group);
        }
    }

    public void removeGroup(StudentGroup group) {
        if (this.groups.contains(group)) {
            this.groups.remove(group);
        }
    }
}
