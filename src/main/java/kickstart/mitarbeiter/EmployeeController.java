
package kickstart.mitarbeiter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import java.util.ArrayList;

@Controller
public class EmployeeController {

	private EmployeeRepository employeeRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private UserAccountManagement userAccountManager;
	@Autowired
	private HolidayRequestRepository holidayRequestRepository;
	private final PaycheckRepository paycheckRepository;
	private final CustomUserDetailsService customUserDetailsService;

	@Autowired
	public EmployeeController(EmployeeRepository employeeRepository, PaycheckRepository paycheckRepository, // Add this line
			UserAccountManagement userAccountManager, CustomUserDetailsService customUserDetailsService) {
		this.employeeRepository = employeeRepository;
		this.paycheckRepository = paycheckRepository;
		this.userAccountManager = userAccountManager;
		this.customUserDetailsService = customUserDetailsService;
		// createBossUser();
	}

//	@GetMapping("/")
//	public String index() {
//		return "login";
//	}

	@GetMapping("/employees")
	public String listEmployees(Model model) {
		model.addAttribute("employees", employeeRepository.findAll());
		return "employees";
	}

	@GetMapping("/employees/new")
	public String newEmployee(Model model) {
	    Employee employee = new Employee();
	    model.addAttribute("employee", employee);
	    return "new-employee";
	}

	@PostMapping("/create")
	public String createEmployee(RedirectAttributes redirectAttrs, @ModelAttribute("employee") Employee employee, @RequestParam String username,
	        @RequestParam String password) {
	    Employee existingEmployee = employeeRepository.findByUsername(username);
	    if (existingEmployee != null || username.equalsIgnoreCase("boss")) {
	        redirectAttrs.addFlashAttribute("error", "Der Benutzername ist bereits vergeben oder unzulässig.");
	        return "redirect:/employees/new";
	    }
	    employee.setRole(Arrays.asList("EMPLOYEE")); 
	    employee.setUsername(username);
	    employee.setPassword(password);
	    
	    employeeRepository.save(employee);

	    UserAccount userAccount = userAccountManager.create(username, Password.UnencryptedPassword.of(password), Role.of("EMPLOYEE"));
	    userAccountManager.save(userAccount);

	    return "redirect:/employees";
	}

	


	@PostMapping("/employees/{name}")
	public String removeEmployee(@PathVariable("name") String name,
	                             @RequestParam("username") String username,
	                             @RequestParam("password") String password) {
	    Employee employee = employeeRepository.findByNameAndUsername(name, username);
	    if (employee != null && employee.getPassword().equals(password)) {
	        // Lösche alle Schedules des Mitarbeiters
	        List<Schedule> schedules = scheduleRepository.findByEmployee(employee);
	        scheduleRepository.deleteAll(schedules);

	        // Lösche alle Holiday Requests des Mitarbeiters
	        List<HolidayRequest> holidayRequests = holidayRequestRepository.findByEmployee(employee);
	        holidayRequestRepository.deleteAll(holidayRequests);
	        List<Paycheck> paychecks = paycheckRepository.findByEmployee(employee);
	        paycheckRepository.deleteAll(paychecks);

	        // Lösche den Mitarbeiter
	        employeeRepository.delete(employee);

	        // Lösche das Benutzerkonto
	        Optional<UserAccount> userAccountOptional = userAccountManager.findByUsername(username);
	        if (userAccountOptional.isPresent()) {
	            UserAccount userAccount = userAccountOptional.get();
	            userAccountManager.delete(userAccount);
	        }
	    }
	    return "redirect:/employees";
	}




	@PostMapping("/employees/update")
	public String updateEmployee(@ModelAttribute("edit_employee") Employee employee,
			@RequestParam("qualifications") String qualifications, @RequestParam("originalName") String originalName,
			@RequestParam("day") int day, @RequestParam("month") int month, @RequestParam("year") int year) {
		Employee existingEmployee = employeeRepository.findByName(originalName);

		if (existingEmployee != null) {
			existingEmployee.setName(employee.getName());
			// existingEmployee.setAge(employee.getAge());
			existingEmployee.setGender(employee.getGender());
			existingEmployee.setAddress(employee.getAddress());
			existingEmployee.setSalary(employee.getSalary());

			List<String> qualificationsList = new ArrayList<>(Arrays.asList(qualifications.split(", ")));
			existingEmployee.setQualifications(qualificationsList);

			existingEmployee.setDay(day);
			existingEmployee.setMonth(month);
			existingEmployee.setYear(year);

			employeeRepository.save(existingEmployee);
		}

		return "redirect:/employees";
	}

	@GetMapping("/employees/edit-employee/{name}")
	public String editEmployee(@PathVariable String name, Model model) {
		Optional<Employee> employee = Optional.ofNullable(employeeRepository.findByName(name));
		if (employee.isPresent()) {
			model.addAttribute("employee", employee.get());
			return "edit-employee";
		} else {
			return "error";
		}
	}

	@PostMapping("/employees/{name}/request-holiday")
	public String requestHoliday(@PathVariable String name, @RequestParam String startDate,
	        @RequestParam int duration) {

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    LocalDate date = LocalDate.parse(startDate, formatter);

	    Employee employee = employeeRepository.findByName(name);
	    if (employee != null) {
	        HolidayRequest request = new HolidayRequest();
	        request.setEmployee(employee);
	        request.setStartDate(date); 
	        request.setDuration(duration);
	        request.setStatus("ÖFFEN");
	        holidayRequestRepository.save(request);
	    }
	    return "redirect:/employeeView/" + employee.getUsername();
	}
	
	@GetMapping("/employeeView/{name}")
    public String employeeView(@PathVariable String name, Model model, Authentication authentication) {
        Employee employee = employeeRepository.findByUsername(name);

        if (employee != null) {
            model.addAttribute("employee", employee);
            model.addAttribute("username", employee.getUsername());
            model.addAttribute("password", employee.getPassword()); 

            // Überprüfe, ob der Trainer die Rolle "Trainer" hat
            boolean isTrainer = authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TRAINER"));
            model.addAttribute("isTrainer", isTrainer);
        } else {
            model.addAttribute("employee", new Employee());
        }

        return "employeeView";
    }
	@GetMapping("/employeeView/{name}/personal-information")
	public String showPersonalInformation(@PathVariable String name, Model model) {
	    Employee employee = employeeRepository.findByName(name);
	    if (employee != null) {
	        model.addAttribute("employee", employee);
	        return "employees-details";
	    } else {
	        return "error";
	    }
	}
	
	@PostMapping("/createHolidayRequest")
	public String createHolidayRequest(@RequestParam String startDate,
	                                   @RequestParam int duration,
	                                   @RequestParam("employeeName") String employeeName) {
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    LocalDate date = LocalDate.parse(startDate, formatter);

	    Employee employee = employeeRepository.findByName(employeeName);
	    if (employee != null) {
	        HolidayRequest request = new HolidayRequest();
	        request.setEmployee(employee);
	        request.setStartDate(date);
	        request.setDuration(duration);
	        request.setStatus("ÖFFEN");
	        holidayRequestRepository.save(request);
	    }
	    return "redirect:/employeeView/" + employee.getUsername();
	}

	@GetMapping("/holiday-requests")
	public String listHolidayRequests(Model model) {
		model.addAttribute("requests", holidayRequestRepository.findByStatus("ÖFFEN"));
		return "holiday-requests";
	}

	@PostMapping("/holiday-requests/{id}/approve")
	public String approveHolidayRequest(@PathVariable Long id) {
		HolidayRequest request = holidayRequestRepository.findById(id).orElse(null);
		if (request != null) {
			request.setStatus("GENEHMIGT");
			holidayRequestRepository.save(request);
		}
		return "redirect:/holiday-requests";
	}

	@GetMapping("/approved-requests")
	public String listApprovedHolidayRequests(Model model, Authentication authentication) {
	    String username = authentication.getName(); // Get the username of the currently logged-in user

	    Employee employee = employeeRepository.findByUsername(username);
	    List<HolidayRequest> approvedRequests = holidayRequestRepository.findByStatus("GENEHMIGT");

	    // Filter the approved requests to include only the ones for the current employee
	    List<HolidayRequest> filteredRequests = approvedRequests.stream()
	            .filter(request -> request.getEmployee().equals(employee))
	            .collect(Collectors.toList());

	    model.addAttribute("approvedRequests", filteredRequests);
	    return "approved-requests";
	}

	@GetMapping("/rejected-requests")
	public String listRejectedHolidayRequests(Model model, Authentication authentication) {
	    String username = authentication.getName(); // Benutzername des aktuellen angemeldeten Benutzers

	    Employee employee = employeeRepository.findByUsername(username);
	    List<HolidayRequest> rejectedRequests = holidayRequestRepository.findByStatus("ABGELEHNT");

	    // Filtern der abgelehnten Anträge, um nur den Antrag des aktuellen Mitarbeiters einzuschließen
	    List<HolidayRequest> filteredRequests = rejectedRequests.stream()
	            .filter(request -> request.getEmployee().equals(employee))
	            .collect(Collectors.toList());

	    model.addAttribute("rejectedRequests", filteredRequests);
	    return "rejected-requests";
	}


	@PostMapping("/holiday-requests/{id}/delete")
	public String deleteHolidayRequest(@PathVariable Long id) {
		holidayRequestRepository.deleteById(id);
		return "redirect:/rejected-requests";
	}

	@PostMapping("/holiday-requests/{id}/delete-approved")
	public String deleteApprovedHolidayRequest(@PathVariable Long id) {
		holidayRequestRepository.deleteById(id);
		return "redirect:/approved-requests";
	}

	@PostMapping("/holiday-requests/{id}/reject")
	public String rejectHolidayRequest(@PathVariable Long id) {
		HolidayRequest request = holidayRequestRepository.findById(id).orElse(null);
		if (request != null) {
			request.setStatus("ABGELEHNT");
			holidayRequestRepository.save(request);
		}
		return "redirect:/holiday-requests";
	}
	@GetMapping("/employeeView/{name}/request-holiday")
	public String showRequestHolidayForm1(@PathVariable String name, Model model) {
	    Employee employee = employeeRepository.findByName(name);
	    if (employee != null) {
	        HolidayRequest request = new HolidayRequest();
	        request.setEmployee(employee);
	        model.addAttribute("request", request);
	        model.addAttribute("employee", employee);
	        return "request-holiday";
	    } else {
	        return "error";
	    }
	}
	@GetMapping("/employeeView/{name}/schedule")
	public String showEmployeeSchedule(@PathVariable String name, Model model) {
	    Employee employee = employeeRepository.findByName(name);
	    if (employee != null) {
	        List<Schedule> schedules = scheduleRepository.findAll().stream()
	                .filter(schedule -> schedule.getEmployee() != null && schedule.getEmployee().getId().equals(employee.getId()))
	                .collect(Collectors.toList());
	        model.addAttribute("schedules", schedules);
	        model.addAttribute("employee", employee);
	        return "schedule";
	    } else {
	        return "error";
	    }
	}


	@PostMapping("/employees/{name}/add-paycheck")
	public String addPaycheckToEmployee(@PathVariable String name, @ModelAttribute("paycheck") Paycheck paycheck,
			Model model) {
		Employee employee = employeeRepository.findByName(name);
		if (employee != null) {
			paycheck.setEmployee(employee);
			paycheckRepository.save(paycheck);
			model.addAttribute("employee", employee);
			model.addAttribute("paycheck", paycheck);
			return "employee-details";
		}
		return "error";
	}
	@GetMapping("/employees/{name}/paycheck")
	public String viewPaycheckDetails(@PathVariable String name, Model model) {
	    Employee employee = employeeRepository.findByName(name);
	    if (employee != null) {
	        List<Paycheck> paychecks = paycheckRepository.findByEmployee(employee);
	        model.addAttribute("employee", employee);
	        model.addAttribute("paychecks", paychecks);
	        return "paycheck-details";
	    }
	    return "error";
	}


	@GetMapping("/employees/{name}/download")
	public ResponseEntity<Resource> downloadEmployeeDetails(@PathVariable String name)
	        throws DocumentException, IOException {
	    Employee employee = employeeRepository.findByName(name);
	    Paycheck paycheck = paycheckRepository.findFirstByEmployeeOrderByIdDesc(employee);

	    if (employee == null) {
	        return ResponseEntity.notFound().build();
	    }

	    File tempFile = File.createTempFile("employee-details", ".pdf");

	    Document document = new Document();
	    PdfWriter.getInstance(document, new FileOutputStream(tempFile));
	    document.open();

	    Paragraph title = new Paragraph("Mitarbeiterdetails",
	            FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLDITALIC));
	    document.add(title);

	    // Add more details here
	    Paragraph nameParagraph = new Paragraph("Name: " + employee.getName());
	    document.add(nameParagraph);
	    Paragraph address = new Paragraph("Adresse: " + employee.getAddress());
	    document.add(address);
	    Paragraph salary = new Paragraph("Gehalt: " + employee.getSalary());
	    document.add(salary);
	    Paragraph genderPara = new Paragraph("Geschlecht: " + employee.getGender());
	    document.add(genderPara);
	    Paragraph agePara = new Paragraph(
	            "Alter: " + employee.getDay() + "." + employee.getMonth() + "." + employee.getYear());
	    document.add(agePara);
	    Paragraph qualificationsPara = new Paragraph(
	            "Qualifications: " + String.join(", ", employee.getQualifications()));
	    document.add(qualificationsPara);
	    Paragraph usernamePara = new Paragraph("Benutzername: " + employee.getUsername());
	    document.add(usernamePara);
	    Paragraph passwordPara = new Paragraph("Password: " + employee.getPassword());
	    document.add(passwordPara);
	    Paragraph paycheckPara = new Paragraph("Neuer Gehaltsscheck:");
	    document.add(paycheckPara);
	    Paragraph salaryPara = new Paragraph("Gehalt: " + paycheck.getSalary());
	    document.add(salaryPara);
	    Paragraph overtimePara = new Paragraph("ÜberStunde: " + paycheck.getOvertime());
	    document.add(overtimePara);
	    Paragraph bonusesPara = new Paragraph("Bonuses: " + paycheck.getBonuses());
	    document.add(bonusesPara);
	   

	    document.close();

	    HttpHeaders headers = new HttpHeaders();
	    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee-details.pdf");

	    Path path = Paths.get(tempFile.getAbsolutePath());
	    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

	    return ResponseEntity.ok().headers(headers).contentLength(tempFile.length())
	            .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}


	@GetMapping("/employees/{name}/add-paycheck")
	public String showAddPaycheckForm(@PathVariable String name, Model model) {
		Employee employee = employeeRepository.findByName(name);
		if (employee != null) {
			Paycheck paycheck = new Paycheck();
			paycheck.setEmployee(employee);
			model.addAttribute("paycheck", paycheck);
			model.addAttribute("employee", employee);
			return "add-paycheck";
		} else {
			return "error";
		}
	}

	@PostMapping("/paychecks/delete/{id}")
	public String deletePaycheckById(@PathVariable Long id) {
	    paycheckRepository.deleteById(id);
	    return "redirect:/employees";
	}


	@GetMapping("/employees/details/{name}")
	public String getEmployeeDetails(@PathVariable String name, Model model) {
		Employee employee = employeeRepository.findByName(name);
		if (employee != null) {
			model.addAttribute("employee", employee);
			return "employee-details";
		}
		return "error";
	}

//	@PostConstruct
//	public void initEmployees() {
//	    createEmployeeIfNotExists(new Employee("Ali", 1, "Male", "Halle", Arrays.asList("Gut"), 5000, 1, 1, 1990, "ali", "pass"));
//	    createEmployeeIfNotExists(new Employee("Ryada", 2, "Female", "Merseburg", Arrays.asList("Profi"), 6000, 2, 2, 1980, "ryada456", "pass456"));
//	    createEmployeeIfNotExists(new Employee("Sanfor", 3, "Male", "Leipzig", Arrays.asList("Normal"), 7000, 3, 3, 1970, "sanfor789", "pass789"));
//	}

	private void createEmployeeIfNotExists(Employee employee) {
	    Employee existingEmployee = employeeRepository.findByName(employee.getName());
	    if (existingEmployee == null) {
	        employee.setUsername(employee.getUsername());
	        employee.setPassword(employee.getPassword());
	        employeeRepository.save(employee);
	    } else {
	        existingEmployee.setUsername(employee.getUsername());
	        existingEmployee.setPassword(employee.getPassword());
	        employeeRepository.save(existingEmployee);
	    }
	}



}