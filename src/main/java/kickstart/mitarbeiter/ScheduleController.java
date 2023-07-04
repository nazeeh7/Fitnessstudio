package kickstart.mitarbeiter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ScheduleController {

    private ScheduleRepository scheduleRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    public ScheduleController(ScheduleRepository scheduleRepository, EmployeeRepository employeeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.employeeRepository = employeeRepository; // Initialisiert
    }

    @GetMapping("/schedules")
    public String showSchedules(Model model) {
    	//Schedule schedule = new Schedule();
        List<String> days = Arrays.asList("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag");
        model.addAttribute("days", days);
        List<Schedule> schedules = scheduleRepository.findAll();
        model.addAttribute("schedules", schedules);
        return "schedules";
    }

    @GetMapping("/schedules/new")
    public String newSchedule(Model model) {
        Schedule schedule = new Schedule();
        List<String> days = Arrays.asList("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag");
        model.addAttribute("days", days);
        model.addAttribute("schedule", schedule);
        model.addAttribute("employees", employeeRepository.findAll());
        return "new-schedule";
    }
    @PostMapping("/schedules/create")
    public String createSchedule(@ModelAttribute("schedule") Schedule schedule,
                                 @RequestParam("day") String[] days,
                                 @RequestParam("startTime") String[] startTimes,
                                 @RequestParam("endTime") String[] endTimes,
                                 @RequestParam("dayCounter") int[] dayCounters,
                                 @RequestParam("employeeName") String employeeName,
                                 @RequestParam("role") String role,
                                 Model model) {
        StringBuilder workingHoursBuilder = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            workingHoursBuilder.append(days[i]);
            workingHoursBuilder.append(", ");
            workingHoursBuilder.append(startTimes[i]);
            workingHoursBuilder.append(" - ");
            workingHoursBuilder.append(endTimes[i]);
            if (i < days.length - 1) {
                workingHoursBuilder.append("; ");
            }
        }
        String workingHours = workingHoursBuilder.toString();
        schedule.setStartTimes(startTimes);
        schedule.setEndTimes(endTimes);
        schedule.setWorkingHours(workingHours);

        Employee employee = employeeRepository.findByName(employeeName);
        if (employee == null) {
            // Mitarbeiter nicht gefunden, daher ggf. eine Fehlerbehandlung durchführen
            // ...
        } else {
            schedule.setEmployee(employee);
        }

        schedule.setRole(role);
        scheduleRepository.save(schedule);

        // Den erstellten Dienstplan dem Modell hinzufügen
        model.addAttribute("createdSchedule", schedule);

        return "redirect:/schedules";
    }

   
    @GetMapping("/schedules/update/{id}")
    public String showUpdateScheduleForm(@PathVariable("id") Long id, Model model) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Schedule-ID: " + id));

        if (schedule.getStartTimes() == null) {
            schedule.setStartTimes(new String[7]); // Oder eine andere Initialisierung des Arrays
        }

        if (schedule.getEndTimes() == null) {
            schedule.setEndTimes(new String[7]); // Initialisierung des endTimes-Arrays
        }

        // Daten für das Formular vorbereiten
        model.addAttribute("schedule", schedule);
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("days", new String[]{"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"});
        model.addAttribute("startTimes", schedule.getStartTimes());
        model.addAttribute("endTimes", schedule.getEndTimes()); // Hinzufügen des endTimes-Arrays

        return "edit-schedule";
    }


    @PostMapping("/schedules/update")
    public String updateSchedule(@ModelAttribute("schedule") Schedule updatedSchedule,
                                 @RequestParam("days") String[] days,
                                 @RequestParam("startTimes") String[] startTimes,
                                 @RequestParam("endTimes") String[] endTimes,
                                 Model model) {
        Schedule existingSchedule = scheduleRepository.findById(updatedSchedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Schedule-ID: " + updatedSchedule.getId()));

        // Mitarbeiter- und Rollendaten beibehalten
        updatedSchedule.setEmployee(existingSchedule.getEmployee());
        updatedSchedule.setRole(existingSchedule.getRole());

        // Aktualisierte Daten in den vorhandenen Dienstplan kopieren
        StringBuilder workingHoursBuilder = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            workingHoursBuilder.append(days[i]);
            workingHoursBuilder.append(", ");
            workingHoursBuilder.append(startTimes[i]);
            workingHoursBuilder.append(" - ");
            workingHoursBuilder.append(endTimes[i]);
            if (i < days.length - 1) {
                workingHoursBuilder.append("; ");
            }
        }
        String workingHours = workingHoursBuilder.toString();
        existingSchedule.setWorkingHours(workingHours);
        existingSchedule.setStartTimes(startTimes);
        existingSchedule.setEndTimes(endTimes);
        
        // Speichern des aktualisierten Dienstplans in der Datenbank
        scheduleRepository.save(existingSchedule);

        // Den aktualisierten Dienstplan dem Modell hinzufügen
        model.addAttribute("updatedSchedule", existingSchedule);

        return "redirect:/schedules";
    }






    @PostMapping("/schedules/delete/{id}")
    public String removeSchedule(@PathVariable("id") Long id) {
        Optional<Schedule> scheduleOpt = scheduleRepository.findById(id);
        if (scheduleOpt.isPresent()) {
            scheduleRepository.delete(scheduleOpt.get());
        }
        return "redirect:/schedules";
    }


}