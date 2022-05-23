package teacherbook.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.users.School;
import teacherbook.model.users.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    public Teacher findByFullnameAndSchool(String fullname, School school);
}
