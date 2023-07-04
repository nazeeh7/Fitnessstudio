package kickstart.mitarbeiter;

import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import javax.annotation.PostConstruct;

@Component
@Order(20)  // Diese Zahl bestimmt die Reihenfolge der Ausführung. Sie können sie anpassen.
public class EmployeeDataInitializer implements DataInitializer {

    private final UserAccountManagement userAccountManager;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeDataInitializer(UserAccountManagement userAccountManager, EmployeeRepository employeeRepository) {
        this.userAccountManager = userAccountManager;
        this.employeeRepository = employeeRepository;
    }

    @PostConstruct
    public void initData() {
        // Hier wird die Initialisierung durchgeführt
        initialize();
    }

    @Override
    public void initialize() {
        if (userAccountManager.findByUsername("employee1").isPresent()) {
            return;
        }

        UserAccount userAccount = userAccountManager.create("employee1", Password.UnencryptedPassword.of("password"), Role.of("EMPLOYEE"));
        userAccountManager.save(userAccount);

        Employee employee1 = new Employee();
        employee1.setUsername("employee1");
        employee1.setPassword("password");
        employee1.setName("employee1");
        employee1.setDay(1);
        employee1.setMonth(1);
        employee1.setYear(1999);
        employee1.setSalary(1500);
        employee1.setQualifications(Arrays.asList("Gut"));
        employee1.setAddress("Halle Saale");
        employee1.setGender("M");
 
        employeeRepository.save(employee1);
        
        if (userAccountManager.findByUsername("Trainer").isPresent()) {
            return;
        }

        UserAccount userAccount1 = userAccountManager.create("Trainer1", Password.UnencryptedPassword.of("123"), Role.of("TRAINER"));
        userAccountManager.save(userAccount1);

        Employee employee2 = new Employee();
        employee2.setUsername("Trainer1");
        employee2.setPassword("123");
        employee2.setName("John");
        employee2.setDay(1);
        employee2.setMonth(1);
        employee2.setYear(1999);
        employee2.setSalary(1500);
        employee2.setQualifications(Arrays.asList("Gut"));
        employee2.setAddress("Halle Saale");
        employee2.setGender("M");
 
        employeeRepository.save(employee2);
    }
}
