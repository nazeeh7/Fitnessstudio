package kickstart.mitarbeiter;

import java.util.Arrays;
import java.util.List;

import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kickstart.mitglieder.MemberRepository;
@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountManagement userAccountManager;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public CustomUserDetailsService(MemberRepository memberRepository, EmployeeRepository employeeRepository,PasswordEncoder passwordEncoder,UserAccountManagement userAccountManager) {
        this.memberRepository = memberRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAccountManager = userAccountManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername called with username: " + username);

        if (username.equals("boss")) {
            return User.withUsername("boss")
                    .password(passwordEncoder.encode("123"))
                    .roles("ADMIN")
                    .build();
        }
        if (username.equals("Trainer1")) {
            return User.withUsername("Trainer1")
                    .password(passwordEncoder.encode("123"))
                    .roles("TRAINER")
                    .build();
        }
        UserDetails memberDetails = memberRepository.findByUsername(username)
                .map(member -> User.withUsername(member.getUsername())
                        .password(passwordEncoder.encode(member.getPassword()))
                        .roles("USER")
                        .build())
                .orElse(null);

        if (memberDetails != null) {
            return memberDetails;
        }

        Employee employee = employeeRepository.findByUsername(username);

        if (employee != null) {
            log.info("Employee found with username: " + username);
            UserDetails employeeDetails = User.withUsername(employee.getUsername())
                    .password(passwordEncoder.encode(employee.getPassword()))
                    .roles("EMPLOYEE")
                    .build();
            return employeeDetails;
        } else {
            log.info("No employee found with username: " + username);
        }

        throw new UsernameNotFoundException("Username not found: " + username);
    }

    public void createEmployee(Employee employee) {
        UserAccount userAccount = userAccountManager.create(employee.getUsername(),
                Password.UnencryptedPassword.of(employee.getPassword()), Role.of("EMPLOYEE"));
        userAccount.add(Role.of("EMPLOYEE"));
        userAccountManager.save(userAccount);
    }
    public Employee findEmployeeByUsername(String username) {
        return employeeRepository.findByName(username);
    }
    public UserDetails loadUserByUsernameEmployee(String username) throws UsernameNotFoundException {
    	Employee employee = employeeRepository.findByUsername(username);
        if (employee == null) {
            throw new UsernameNotFoundException("Username not found: " + username);
        }

        return User.withUsername(employee.getUsername())
                .password(passwordEncoder.encode(employee.getPassword()))
                .roles("EMPLOYEE")
                .build();
    }
}