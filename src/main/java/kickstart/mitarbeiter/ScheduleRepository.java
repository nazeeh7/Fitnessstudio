package kickstart.mitarbeiter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Schedule findByName(String name);
    Optional<Schedule> findById(Long id);
    List<Schedule> findByEmployee(Employee employee);
}
