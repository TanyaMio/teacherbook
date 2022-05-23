package teacherbook.model.schedulegrid;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
public class CalendarDay implements Comparable<CalendarDay>{

    @Id
    @Column(name="day_id")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="semester_id")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name="rotation_day_id")
    private RotationDay rotation_day;

    @NotNull
    private Date date;

    public CalendarDay() {
        super();
    }

    public CalendarDay(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Semester getSemester() {
        return semester;
    }

    public RotationDay getRotation_day() {
        return rotation_day;
    }

    public Date getDate() {
        return date;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public void setRotation_day(RotationDay rotation_day) {
        this.rotation_day = rotation_day;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int compareTo(CalendarDay o) {
        return this.date.compareTo(o.getDate());
    }

    @Override
    public String toString() {
        String day = "" + this.date.toLocalDate().getDayOfMonth();
        if (this.rotation_day != null) {
            day += "\n" + this.rotation_day.toString();
        }
        return day;
    }
}
