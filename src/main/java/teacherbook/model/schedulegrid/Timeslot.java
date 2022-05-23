package teacherbook.model.schedulegrid;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.constraints.NotNull;

@Entity
public class Timeslot implements Comparable<Timeslot>{

    @Id
    @Column(name="timeslot_id")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String start_time;
    @NotNull
    private String end_time;

    @ManyToOne
    @JoinColumn(name="semester_id")
    private Semester semester;

    public Timeslot() {
        super();
    }

    public Timeslot(String name, @NotNull String start, @NotNull String end) {
        this.name = name;
        this.start_time = start;
        this.end_time = end;
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

    public @NotNull String getStart_time() {
        return start_time;
    }

    public void setStart_time(@NotNull String start_time) {
        this.start_time = start_time;
    }

    public @NotNull String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(@NotNull String end_time) {
        this.end_time = end_time;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    @Override
    public int compareTo(Timeslot o) {
        int hour_this = Integer.parseInt(this.end_time.substring(0, this.end_time.indexOf(":")));
        int hour_o = Integer.parseInt(o.getStart_time().substring(0,o.getStart_time().indexOf(":")));
        if (hour_o-hour_this == 0) {
            int minutes_this = Integer.parseInt(this.end_time.substring(this.end_time.indexOf(":")+1));
            int minutes_o = Integer.parseInt(o.getStart_time().substring(o.getStart_time().indexOf(":")+1));
            return minutes_this-minutes_o;
        } else {
            return hour_this-hour_o;
        }

    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Timeslot && ((Timeslot) o).getId().equals(this.id);
    }
}
