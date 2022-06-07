package teacherbook.repositories.gradebook;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.Course;
import teacherbook.model.StudentGroup;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.schedulegrid.Semester;

import java.util.List;

public interface GradebookRepository  extends JpaRepository<Gradebook, Long> {
    public Gradebook findBySemesterAndGroupAndCourse(Semester semester, StudentGroup group, Course course);
    public List<Gradebook> findAllBySemesterAndGroup(Semester semester, StudentGroup group);
    public List<Gradebook> findAllByGroup(StudentGroup group);
}
