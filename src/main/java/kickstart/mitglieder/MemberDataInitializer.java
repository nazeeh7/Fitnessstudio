package kickstart.mitglieder;

import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@Order(10)  // Diese Zahl bestimmt die Reihenfolge der Ausführung. Sie können sie anpassen.
public class MemberDataInitializer implements DataInitializer {

    private final UserAccountManagement userAccountManager;
    private final MemberRepository memberRepository;
    
    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;


    @Autowired
    public MemberDataInitializer(UserAccountManagement userAccountManager, MemberRepository memberRepository, MemberService memberService) {
        this.userAccountManager = userAccountManager;
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    public void initData() {
        // Hier wird die Initialisierung durchgeführt
        initialize();
    }

    @Override
    public void initialize() {
        if (userAccountManager.findByUsername("johndoe").isPresent()) {
            // Die Mitglieder sind bereits initialisiert
            return;
        }
        // Erstelle einen Admin-Benutzeraccount für den Benutzer "boss"
        UserAccount adminAccount = userAccountManager.create("boss", Password.UnencryptedPassword.of("123"), Role.of("ADMIN"));
        userAccountManager.save(adminAccount);
        // Erstelle einen Benutzeraccount für den Benutzer "johndoe"
        UserAccount userAccount = userAccountManager.create("johndoe", Password.UnencryptedPassword.of("password"), Role.of("USER"));
        userAccountManager.save(userAccount);

        
        
        // Erstelle ein Mitglied mit den entsprechenden Daten
        Member member1 = new Member();
        member1.setUsername("johndoe");
        member1.setPassword("password");
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setAddress("123 Main St");
        member1.setBirthdate(LocalDate.of(1990, 1, 1));
        member1.setGender("M");
        member1.setPhone("123-456-7890");
        member1.setEmail("johndoe@example.com");
        memberRepository.save(member1);



        // Erstelle einen Trainingsplan für member1
        TrainingPlan trainingPlan1 = new TrainingPlan("Anfänger-Plan", member1, 60, new ArrayList<>());
        trainingPlanRepository.save(trainingPlan1);

        // Erstelle Übungen für den Trainingsplan
     // Erstellen der Übungen für jeden Wochentag
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            Exercise exercise = new Exercise();
            exercise.setName("Übung"); // Setze den Namen der Übung entsprechend
            exercise.setDescription("Beschreibung"); // Setze die Beschreibung der Übung entsprechend
            exercise.setDurationInMinutes(10); // Setze die Dauer der Übung entsprechend
            exercise.setRepetitions(10); // Setze die Wiederholungen entsprechend
            exercise.setSets(3); // Setze die Sets entsprechend
            exercise.setDayOfWeek(dayOfWeek);
            exercise.setTrainingPlan(trainingPlan1);

            // Speichern der Übung
            exerciseRepository.save(exercise);

            // Hinzufügen der Übung zum Trainingsplan
            trainingPlan1.getExercises().add(exercise);
        }

        // Speichern des aktualisierten Trainingsplans
        trainingPlanRepository.save(trainingPlan1);


    }


}
