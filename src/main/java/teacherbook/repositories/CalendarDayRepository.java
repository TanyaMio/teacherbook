package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.schedulegrid.CalendarDay;
import teacherbook.model.schedulegrid.RotationDay;
import teacherbook.model.schedulegrid.Semester;

import java.sql.Date;
import java.util.List;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, Long> {
    public List<CalendarDay> findAllBySemester(Semester semester);
    public List<CalendarDay> findAllBySemesterAndRotationday(Semester semester, RotationDay rotationDay);
    public CalendarDay findBySemesterAndDate(Semester semester, Date date);
}
