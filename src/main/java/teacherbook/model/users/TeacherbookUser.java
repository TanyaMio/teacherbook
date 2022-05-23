package teacherbook.model.users;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class TeacherbookUser {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long user_id;

    @NotNull
    @Column(unique=true)
    private String username;

    @NotNull
    private String passhash;

    private boolean enabled;

    private String roles;

    @OneToOne
    @JoinColumn(name="school_id")
    private School school;

    @OneToOne
    @JoinColumn(name="teacher_id")
    private Teacher teacher;

    @OneToOne
    @JoinColumn(name="student_id")
    private Student student;

    @OneToOne
    @JoinColumn(name="parent_id")
    private Parent parent;

    public TeacherbookUser() {
        super();
        this.enabled=true;
    }

    public TeacherbookUser(String Nname, String NpassHash, boolean enable) {
        this.username = Nname;
        this.passhash = NpassHash;
        this.enabled = enable;
    }

    public Long getId() {
        return user_id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String email) {
        this.username = email;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

}
