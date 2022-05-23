package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.schedulegrid.Semester;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
}
