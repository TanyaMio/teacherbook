package teacherbook.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import teacherbook.model.schedulegrid.RotationDay;

public interface RotationDayRepository extends JpaRepository<RotationDay, Long> {
}
