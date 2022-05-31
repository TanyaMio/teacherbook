package teacherbook.repositories.gradebook;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.gradebook.Assignment;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.gradebook.Homework;

import java.sql.Date;
import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    public Homework findByAssignment(Assignment assignment);
    public List<Homework> findAllByGradebookAndDuedateAfterAndDuedateBefore(Gradebook gradebook, Date after, Date before);
    public List<Homework> findAllByGradebook(Gradebook gradebook);
}
