package teacherbook.model.users;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Teacher{

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long teacher_id;

    @OneToOne(mappedBy = "teacher")
    private TeacherbookUser user;

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

    public String getFullName() {
        if (this.honorific != null) {
            return this.honorific + " " + this.first_name + " " + this.last_name;
        } else {
            return this.first_name + " " + this.last_name;
        }
    }

    public Teacher(String first_name, String last_name) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.fullname = this.getFullName();
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
}
