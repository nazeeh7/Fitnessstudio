package kickstart.mitglieder;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kickstart.mitarbeiter.Employee;
import kickstart.mitarbeiter.EmployeeRepository;

import java.time.DayOfWeek;
import java.util.*;

@Controller
@RequestMapping("/training-plans")
public class TrainingPlanController {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TrainingPlanRepository trainingPlanRepository;

	@Autowired
	private ExerciseRepository exerciseRepository;

	@Autowired
	private MemberService memberService;
	
	@Autowired
	private EmployeeRepository employeeRepository;

	// Methode zum Anzeigen des Trainingsplans für das Mitglied
	@GetMapping("/{id}")
	public String viewTrainingPlan(@PathVariable("id") Long id, Model model) {
		Optional<TrainingPlan> trainingPlanOptional = trainingPlanRepository.findById(id);
		if (trainingPlanOptional.isPresent() && trainingPlanOptional.get().getMember() != null) {
			model.addAttribute("trainingPlan", trainingPlanOptional.get());
			return "training-plan-view";
		} else {
			return "redirect:/";
		}
	}

	@GetMapping("/list-training-plans")
	public String listTrainingPlans(Model model, Authentication authentication) {
		Iterable<TrainingPlan> iterableTrainingPlans = trainingPlanRepository.findAll();
		List<TrainingPlan> trainingPlans = StreamSupport.stream(iterableTrainingPlans.spliterator(), false)
				.collect(Collectors.toList());
		model.addAttribute("trainingPlans", trainingPlans);
		   boolean isTrainer = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TRAINER"));
		    boolean isAdmin = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
		    model.addAttribute("isTrainer", isTrainer);
		    model.addAttribute("isAdmin", isAdmin);
		    // Stelle sicher, dass das employee-Objekt den richtigen Namen hat
		    String name = authentication.getName();
		    Employee employee = employeeRepository.findByUsername(name);
		    model.addAttribute("employee", employee);
		    model.addAttribute("name", name);
		return "list-training-plans";
	}

	@GetMapping("/list-training-plans/{id}")
	public String viewTrainingPlanDetails(@PathVariable("id") Long id, Model model) {
		Optional<TrainingPlan> trainingPlanOptional = trainingPlanRepository.findById(id);
		if (trainingPlanOptional.isPresent()) {
			TrainingPlan trainingPlan = trainingPlanOptional.get();
			model.addAttribute("trainingPlan", trainingPlan);
			return "training-plan-view";
		} else {
			return "redirect:/training-plans/list-training-plans";
		}
	}

	// Methode zum Bearbeiten des Trainingsplans für den Trainer
	@GetMapping("/{id}/edit")
	public String editTrainingPlan(@PathVariable("id") Long id, Model model) {
		Optional<TrainingPlan> trainingPlanOptional = trainingPlanRepository.findById(id);
		if (trainingPlanOptional.isPresent()) {
			TrainingPlan trainingPlan = trainingPlanOptional.get();
			List<String> exerciseNames = Arrays.asList("Push-ups", "Squats", "Lunges", "Pull-ups", "Plank", "Free");
			List<Exercise> exercises = exerciseRepository.findByTrainingPlan(trainingPlan);

			// Retrieve the member from the database
			Member member = memberService.getMemberById(trainingPlan.getMember().getId());

			model.addAttribute("trainingPlan", trainingPlan);
			model.addAttribute("exerciseNames", exerciseNames);
			model.addAttribute("exercises", exercises);
			model.addAttribute("member", member); // Add the member to the model

			return "training-plan-edit";
		} else {
			return "redirect:/";
		}
	}

	@GetMapping("/member-dashboard")
	public String showMemberDashboard() {
		return "member-dashboard";
	}

	// Methode zum Speichern von Änderungen am Trainingsplan
	@PostMapping("/{id}/edit")
	public String saveTrainingPlan(@PathVariable("id") Long id,
	        @ModelAttribute("trainingPlan") TrainingPlan trainingPlan,
	        @RequestParam("exerciseName") List<String> selectedExerciseNames,
	        @RequestParam Map<String, String> exercises) {

	    List<Exercise> existingExercises = exerciseRepository.findByTrainingPlan(trainingPlan);
	    List<Exercise> updatedExercises = new ArrayList<>();
	    DayOfWeek[] daysOfWeek = DayOfWeek.values();

	    // Check if the sizes of selectedExerciseNames and daysOfWeek match
	    if (selectedExerciseNames.size() != daysOfWeek.length) {
	        // Handle the error or display an error message
	        return "redirect://training-plans/list-training-plans?error=listSizeMismatch";
	    }

	    for (int i = 0; i < daysOfWeek.length; i++) {
	        DayOfWeek dayOfWeek = daysOfWeek[i];
	        String selectedExerciseName = selectedExerciseNames.get(i);

	        if (selectedExerciseName != null && !selectedExerciseName.isBlank()) {
	            // Find the exercise by its id
	            Long exerciseId = Long.parseLong(exercises.get("exercises[" + i + "].id"));
	            Exercise existingExercise = exerciseRepository.findById(exerciseId).orElse(null);

	            if (existingExercise != null) {
	                // Exercise already exists, update the data
	                existingExercise.setDayOfWeek(dayOfWeek);
	                existingExercise.setName(selectedExerciseName);
	                existingExercise.setDescription(exercises.get("exercises[" + i + "].description"));
	                existingExercise.setDurationInMinutes(
	                        Integer.parseInt(exercises.get("exercises[" + i + "].durationInMinutes")));
	                updatedExercises.add(existingExercise);
	            } else {
	                // Add new exercise
	                Exercise newExercise = new Exercise();
	                newExercise.setDayOfWeek(dayOfWeek);
	                newExercise.setName(selectedExerciseName);
	                newExercise.setDescription(exercises.get("exercises[" + i + "].description"));
	                newExercise.setDurationInMinutes(
	                        Integer.parseInt(exercises.get("exercises[" + i + "].durationInMinutes")));
	                newExercise.setTrainingPlan(trainingPlan);
	                updatedExercises.add(newExercise);
	            }
	        }
	    }
	    // Save updated exercises
	    trainingPlan.getExercises().clear();
	    trainingPlan.getExercises().addAll(updatedExercises);
	    trainingPlanRepository.save(trainingPlan);

	    return "redirect:/training-plans/list-training-plans";
	}


	private Exercise findExerciseByDayAndName(List<Exercise> exercises, DayOfWeek dayOfWeek, String exerciseName) {
		for (Exercise exercise : exercises) {
			if (exercise.getDayOfWeek() == dayOfWeek && exercise.getName().equals(exerciseName)) {
				return exercise;
			}
		}
		return null;
	}

	@PostMapping("/save")
	public String saveTrainingPlan(@ModelAttribute("trainingPlan") TrainingPlan trainingPlan,
			@RequestParam("exerciseName") List<String> selectedExerciseNames,
			@RequestParam Map<String, String> exercises) {
		List<Exercise> exerciseList = new ArrayList<>();
		DayOfWeek[] daysOfWeek = DayOfWeek.values();

		 // Check if member is not null before getting its id
	    if (trainingPlan.getMember() != null) {
	        Optional<Member> memberOptional = memberRepository.findById(trainingPlan.getMember().getId());
	        if (memberOptional.isPresent()) {
	            Member member = memberOptional.get();

	            // Add this check
	            if (member.getTrainingPlan() != null) {
	                // Member already has a training plan, return an error or redirect
	                return "redirect:/training-plans/new?error=memberHasTrainingPlan";
	            }

	            trainingPlan.setMember(member);
	        } else {
	            // Handle case when member is not found
	            return "redirect:/training-plans/new?error=memberNotFound";
	        }
	    } else {
	        // Handle case when member is null
	        return "redirect:/training-plans/new?error=memberIsNull";
	    }

		for (int i = 0; i < daysOfWeek.length; i++) {
			DayOfWeek dayOfWeek = daysOfWeek[i];
			String selectedExerciseName = selectedExerciseNames.get(i);

			if (selectedExerciseName != null && !selectedExerciseName.isBlank()) {
				Exercise exercise = new Exercise();
				exercise.setDayOfWeek(dayOfWeek);
				exercise.setName(selectedExerciseName);
				exercise.setDescription(exercises.get("exercises[" + i + "].description"));
				exercise.setDurationInMinutes(
						Integer.parseInt(exercises.get("exercises[" + i + "].durationInMinutes")));
				exercise.setTrainingPlan(trainingPlan);
				exerciseList.add(exercise);
			}
		}

		trainingPlan.setExercises(exerciseList);
		trainingPlanRepository.save(trainingPlan);

		return "redirect:/training-plans/list-training-plans";
	}

	@GetMapping("/new")
	public String showNewTrainingPlanForm(Model model) {
	    TrainingPlan trainingPlan = new TrainingPlan();
	    model.addAttribute("trainingPlan", trainingPlan);

	    List<Member> members = (List<Member>) memberRepository.findAll();

	    // Filter out members that already have a training plan
	    List<Member> membersWithoutTrainingPlan = members.stream()
	        .filter(member -> member.getTrainingPlan() == null)
	        .collect(Collectors.toList());

	    model.addAttribute("members", membersWithoutTrainingPlan);

	    List<String> exerciseNames = Arrays.asList("Push-ups", "Squats", "Lunges", "Pull-ups", "Plank", "Free");
	    model.addAttribute("exerciseNames", exerciseNames);

	    model.addAttribute("dayOfWeekValues", DayOfWeek.values());
	    model.addAttribute("exerciseName", "");

	    return "new_training_plan";
	}


	// Methode zum Löschen eines Trainingsplans
	@GetMapping("/{id}/delete")
	public String deleteTrainingPlan(@PathVariable("id") Long id) {
	    Optional<TrainingPlan> trainingPlanOptional = trainingPlanRepository.findById(id);
	    if (trainingPlanOptional.isPresent()) {
	        TrainingPlan trainingPlan = trainingPlanOptional.get();

	        // delete all associated exercises first
	        exerciseRepository.deleteAll(trainingPlan.getExercises());

	        // nullify the reference from Member to TrainingPlan
	        if (trainingPlan.getMember() != null) {
	            Member member = trainingPlan.getMember();
	            member.setTrainingPlan(null);
	            memberRepository.save(member);
	        }

	        // then delete the training plan
	        trainingPlanRepository.delete(trainingPlan);
	        return "redirect:/training-plans/list-training-plans";
	    } else {
	        // handle case when training plan is not found
	        return "redirect:/training-plans/list-training-plans";
	    }
	}



}
