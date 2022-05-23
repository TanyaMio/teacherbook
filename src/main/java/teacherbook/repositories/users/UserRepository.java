package teacherbook.repositories.users;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.users.School;
import teacherbook.model.users.TeacherbookUser;

public interface UserRepository extends JpaRepository<TeacherbookUser, Long> {
    TeacherbookUser findByUsername(String username);
    TeacherbookUser findByUsernameAndSchool(String username, School school);
}
