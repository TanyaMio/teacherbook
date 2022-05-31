package teacherbook.repositories.gradebook;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.gradebook.Assignment;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.gradebook.GradebookEntry;
import teacherbook.model.users.Student;

import java.util.List;

public interface GradebookEntryRepository extends JpaRepository<GradebookEntry, Long> {
    public GradebookEntry findByAssignmentAndStudent(Assignment assignment, Student student);
    public List<GradebookEntry> findAllByGradebookAndStudent(Gradebook gradebook, Student student);
}
