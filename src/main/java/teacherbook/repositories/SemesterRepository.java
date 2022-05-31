package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.schedulegrid.Semester;
import teacherbook.model.users.School;

import java.sql.Date;
import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    public List<Semester> findAllBySchoolAndStartDateBeforeAndEndDateAfter(School school, Date start_date, Date end_date);
    public List<Semester> findAllBySchoolAndEndDateAfter(School school, Date endDate);
}
