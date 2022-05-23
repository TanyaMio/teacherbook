package teacherbook.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.users.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
