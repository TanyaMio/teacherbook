package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.StudentGroup;
import teacherbook.model.schedulegrid.RotationDay;
import teacherbook.model.schedulegrid.ScheduleEntry;
import teacherbook.model.schedulegrid.Semester;
import teacherbook.model.users.Teacher;

import java.util.List;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
    public List<ScheduleEntry> findAllBySemesterAndGroupAndRotationDay(Semester semester, StudentGroup group, RotationDay rotationDay);
    public List<ScheduleEntry> findAllBySemesterAndTeacherAndRotationDay(Semester semester, Teacher teacher, RotationDay rotationDay);
}