package kickstart.mitglieder;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CancellationRequestRepository extends CrudRepository<CancellationRequest, Long> {
	List<CancellationRequest> findAll();
	
	@Query("SELECT cr FROM CancellationRequest cr WHERE cr.member = :member AND cr.approval = :approval")
    List<CancellationRequest> findByMemberAndApproval(@Param("member") Member member, @Param("approval") String approval);
	
	 @Query("SELECT cr FROM CancellationRequest cr WHERE cr.member = :member")
	    List<CancellationRequest> findByMember(@Param("member") Member member);
	 
	 @Query("SELECT cr FROM CancellationRequest cr WHERE cr.contract = :contract")
	    List<CancellationRequest> findByContract(@Param("contract") MembershipContract contract);
	 
	 void deleteByMember(Member member);
	 
	 List<CancellationRequest> findByProcessed(boolean processed);
}
