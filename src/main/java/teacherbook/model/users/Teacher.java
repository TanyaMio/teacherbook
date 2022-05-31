package teacherbook.model.users;

import teacherbook.model.gradebook.Gradebook;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class Teacher{

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long teacher_id;

    @OneToOne(mappedBy = "teacher")
    private TeacherbookUser TBuser;

    @NotNull
    private String fullname;

    private String honorific;

    @NotNull
    private String first_name;

    @NotNull
    private String last_name;

    @ManyToOne
    @JoinColumn(name="school_id")
    private School school;

    @ManyToMany
    @JoinTable(
            name = "teacher_gradebooks",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "gradebook_id"))
    private Collection<Gradebook> gradebooks;

    public String getFullName() {
        if (this.honorific != null) {
            return this.honorific + " " + this.first_name + " " + this.last_name;
        } else {
            return this.first_name + " " + this.last_name;
        }
    }

    public Teacher(String first_name, String last_name) {
        this.honorific = "";
        this.first_name = first_name;
        this.last_name = last_name;
        this.fullname = this.getFullName().strip();
    }

    public Teacher() {
        super();
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Long getTeacher_id() {
        return teacher_id;
    }

    public String getHonorific() {
        return honorific;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getFullname() {
        return fullname;
    }

    public TeacherbookUser getTBuser() {
        return TBuser;
    }

    public Collection<Gradebook> getGradebooks() {return gradebooks;}

    public void addGradebook(Gradebook g) {
        if (!this.gradebooks.contains(g)) {
            this.gradebooks.add(g);
        }
    }

    public void removeGradebook(Gradebook g) {
        if (this.gradebooks.contains(g)) {
            this.gradebooks.remove(g);
        }
    }

    public void setHonorific(String honorific) {
        this.honorific = honorific;
        this.fullname = this.getFullName();
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
        this.fullname = this.getFullName();
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
        this.fullname = this.getFullName();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Teacher && ((Teacher) o).getTeacher_id().equals(this.teacher_id);
    }
}
