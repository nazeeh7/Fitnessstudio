package kickstart.mitarbeiter;

import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class EmployeeService {

    private final UserAccountManagement userAccountManager;
    private final EmployeeRepository employeeRepository;
    private final CustomUserDetailsService customUserDetailsService;
    
    @Autowired
    public EmployeeService(UserAccountManagement userAccountManager, EmployeeRepository employeeRepository, CustomUserDetailsService customUserDetailsService) {
        this.userAccountManager = userAccountManager;
        this.employeeRepository = employeeRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    public Employee createEmployee(String name, String username, String password) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setUsername(username);
        employee.setPassword(password);

        employeeRepository.save(employee);

        customUserDetailsService.createEmployee(employee); // Rufen Sie die Methode in CustomUserDetailsService auf, um die Rolle zu setzen

        return employee;
    }

 


    public Optional<Employee> getEmployeeByName(String name) {
        return Optional.ofNullable(employeeRepository.findByName(name));
    }
}
