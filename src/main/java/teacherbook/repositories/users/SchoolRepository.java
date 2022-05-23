package teacherbook.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.users.School;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
