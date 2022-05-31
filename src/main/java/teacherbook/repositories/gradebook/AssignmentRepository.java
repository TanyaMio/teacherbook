package teacherbook.repositories.gradebook;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.gradebook.Assignment;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.schedulegrid.CalendarDay;
import teacherbook.model.schedulegrid.Timeslot;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    public Assignment findByGradebookAndAndDayAndTimeslot(Gradebook gradebook, CalendarDay day, Timeslot timeslot);
    public List<Assignment> findAllByGradebookAndIsHomework(Gradebook gradebook, boolean isHomework);
}
