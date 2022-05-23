package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.schedulegrid.Semester;
import teacherbook.model.schedulegrid.Timeslot;

import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {
    List<Timeslot> findAllBySemester(Semester semester);
}
