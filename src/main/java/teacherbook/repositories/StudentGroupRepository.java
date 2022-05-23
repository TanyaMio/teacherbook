package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.StudentGroup;
import teacherbook.model.users.School;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    public StudentGroup findByNameAndSchool(String name, School school);
}
