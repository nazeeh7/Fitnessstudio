package kickstart.mitglieder;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface MembershipContractRepository extends JpaRepository<MembershipContract, Long> {
	List<MembershipContract> findByStatus(String status);
}
