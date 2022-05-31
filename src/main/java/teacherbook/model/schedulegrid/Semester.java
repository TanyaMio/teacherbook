package teacherbook.model.schedulegrid;

import teacherbook.model.users.School;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

@Entity
public class Semester {
    @Id
    @Column(name="semester_id")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="school_id")
    private School school;

    @NotNull
    private String name;

    @NotNull
    private Date startDate;
    @NotNull
    private Date endDate;
    @NotNull
    private String workdays;

    @NotNull
    private int rotation_length;
    @OneToMany(mappedBy = "semester", orphanRemoval = true)
    private Collection<RotationDay> rotation;

    @OneToMany(mappedBy = "semester", orphanRemoval = true)
    private Collection<CalendarDay> calendar;

    @OneToMany (mappedBy = "semester", orphanRemoval = true)
    private Collection<Timeslot> timeslots;

    public Semester() {
        super();
    }

    public Semester(String name, Date start_date, Date end_date, int rotation_length, String workdays) {
        this.name = name;
        this.startDate = start_date;
        this.endDate = end_date;
        this.rotation_length = rotation_length;
        this.workdays = workdays;
        this.calendar = new ArrayList<CalendarDay>();
    }

    public Long getId() {
        return id;
    }

    public School getSchool() {
        return school;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getWorkdays() {
        return workdays;
    }

    public Collection<CalendarDay> getCalendar() {
        return calendar;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public void setStartDate(Date start_date) {
        this.startDate = start_date;
    }

    public void setEndDate(Date end_date) {
        this.endDate = end_date;
    }

    public void setWorkdays(String workdays) {
        this.workdays = workdays;
    }

    public void setCalendar(Collection<CalendarDay> calendar) {
        this.calendar = calendar;
    }

    public void addToCalendar(CalendarDay day) {
        this.calendar.add(day);
    }

    public int getRotation_length() {
        return rotation_length;
    }

    public void setRotation_length(int rotation_length) {
        this.rotation_length = rotation_length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<RotationDay> getRotation() {
        return rotation;
    }

    public Collection<Timeslot> getTimeslots() {
        return timeslots;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Semester && ((Semester) o).getId().equals(this.id);
    }
}
