package kickstart.mitarbeiter;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRequestRepository extends JpaRepository<HolidayRequest, Long> {
    List<HolidayRequest> findByStatus(String status);
    List<HolidayRequest> findByEmployee(Employee employee);

}
