package kickstart.mitglieder;


import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TrainingPlanRepository extends CrudRepository<TrainingPlan, Long> {
	Optional<TrainingPlan> findByMember(Member member);
	void deleteByMemberId(Long memberId);
}