package teacherbook.model.gradebook;

import teacherbook.model.schedulegrid.CalendarDay;
import teacherbook.model.schedulegrid.Timeslot;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class Assignment implements Comparable<Assignment>{

    @Id
    @Column(name = "assignment_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @ManyToOne
    @JoinColumn(name="gradebook_id")
    private Gradebook gradebook;

    private String topic;

    private boolean isHomework;

    @OneToOne(mappedBy = "assignment")
    private Homework homework;

    @ManyToOne
    @JoinColumn(name="day_id")
    private CalendarDay day;

    @ManyToOne
    @JoinColumn(name="timeslot_id")
    private Timeslot timeslot;

    @OneToMany(mappedBy = "assignment")
    private Collection<GradebookEntry> entries;

    public Assignment() {
        super();
    }

    public Assignment(String name, Gradebook g, boolean hw, CalendarDay day, Timeslot t) {
        this.name = name;
        this.gradebook = g;
        this.isHomework = hw;
        this.day = day;
        this.timeslot = t;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Gradebook getGradebook() {
        return gradebook;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isIsHomework() {
        return isHomework;
    }

    public Homework getHomework() {
        return homework;
    }

    public CalendarDay getDay() {
        return day;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Collection<GradebookEntry> getEntries() {return entries;}

    public void setName(String name) {
        this.name = name;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setIsHomework(boolean is_homework) {
        this.isHomework = is_homework;
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
    }

    @Override
    public int compareTo(Assignment a) {
        int cmpDay = this.day.compareTo(a.getDay());
        if (cmpDay == 0 && this.timeslot != null && a.getTimeslot() != null) {
            return this.timeslot.compareTo(a.getTimeslot());
        }
        return cmpDay;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Assignment && ((Assignment) o).getId().equals(this.id);
    }

    public void setDay(CalendarDay day) {
        this.day = day;
    }
}
