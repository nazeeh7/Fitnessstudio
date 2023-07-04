package kickstart.mitarbeiter;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaycheckRepository extends JpaRepository<Paycheck, Long> {

	Paycheck findFirstByEmployeeOrderByIdDesc(Employee employee);
	 void deleteById(Long id);
	 void deleteByEmployee(Employee employee);
	 List<Paycheck> findByEmployee(Employee employee);

}

