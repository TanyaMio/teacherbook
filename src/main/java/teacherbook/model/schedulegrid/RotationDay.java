package teacherbook.model.schedulegrid;

import teacherbook.model.schedulegrid.CalendarDay;
import teacherbook.model.users.School;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class RotationDay implements Comparable<RotationDay>{

    @Id
    @Column(name="rotation_day_id")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private int num_in_seq;

    @ManyToOne
    @JoinColumn(name="semester_id")
    private Semester semester;


    public RotationDay() {
        super();
    }

    public RotationDay(String name, int NinSeq) {
        this.name = name;
        this.num_in_seq = NinSeq;
    }

    @OneToMany(mappedBy = "rotation_day")
    private Collection<CalendarDay> calendar_days;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNum_in_seq() {
        return num_in_seq;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNum_in_seq(int num_in_seq) {
        this.num_in_seq = num_in_seq;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(RotationDay o) {
        return num_in_seq - o.num_in_seq;
    }
}
