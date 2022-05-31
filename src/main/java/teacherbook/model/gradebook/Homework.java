package teacherbook.model.gradebook;

import teacherbook.model.StudentGroup;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
public class Homework implements Comparable<Homework>{

    @Id
    @Column(name="homework_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String content;

    @NotNull
    private Date duedate;

    @ManyToOne
    @JoinColumn(name="gradebook_id")
    private Gradebook gradebook;

    @ManyToOne
    @JoinColumn(name="group_id")
    private StudentGroup group;

    @OneToOne
    @JoinColumn(name="assignment_id")
    private Assignment assignment;

    private boolean has_column;

    @OneToOne
    @JoinColumn(name="assignment_hw")
    private Assignment column;

    public Homework() {
        super();
    }

    public Homework(String c, Date d, Gradebook g, StudentGroup gr, Assignment a, boolean has_column) {
        this.content = c;
        this.duedate = d;
        this.gradebook = g;
        this.group = gr;
        this.assignment = a;
        this.has_column = has_column;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Date getDuedate() {
        return duedate;
    }

    public Gradebook getGradebook() {
        return gradebook;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public boolean isHas_column() {
        return has_column;
    }

    public Assignment getColumn() {
        return column;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDuedate(Date duedate) {
        this.duedate = duedate;
    }

    public void setHas_column(boolean has_column) {
        this.has_column = has_column;
    }

    public void setColumn(Assignment column) {
        this.column = column;
    }


    @Override
    public int compareTo(Homework o) {
        return this.duedate.compareTo(o.duedate);
    }
}
