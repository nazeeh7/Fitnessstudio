package kickstart.mitglieder;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ExerciseRepository extends CrudRepository<Exercise, Long> {
	List<Exercise> findByTrainingPlan(TrainingPlan trainingPlan);
	Optional<Exercise> findByName(String name);
}