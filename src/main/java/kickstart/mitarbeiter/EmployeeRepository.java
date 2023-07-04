package kickstart.mitarbeiter;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	Employee findByName(String name);	
	Employee findByUsername(String username);
	
	Employee findByNameAndUsername(String name, String username);
}