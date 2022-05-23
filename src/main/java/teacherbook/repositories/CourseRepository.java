package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.Course;
import teacherbook.model.users.School;

public interface CourseRepository extends JpaRepository<Course, Long> {
    public Course findByNameAndSchool(String name, School school);
}
