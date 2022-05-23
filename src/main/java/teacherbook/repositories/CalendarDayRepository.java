package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.schedulegrid.CalendarDay;
import teacherbook.model.schedulegrid.Semester;

import java.util.List;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, Long> {
    public List<CalendarDay> findAllBySemester(Semester semester);
}
