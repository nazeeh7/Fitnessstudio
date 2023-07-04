package kickstart.mitarbeiter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String workingHours;
    private String role;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    private String[] startTimes;
    private String[] endTimes;

    public Schedule() {
    }

    public Schedule(String name, Employee employee, String workingHours, String role, String[] startTimes, String[] endTimes) {
        this.name = name;
        this.employee = employee;
        this.workingHours = workingHours;
        this.role = role;
        this.startTimes = startTimes;
        this.endTimes = endTimes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String[] getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(String[] startTimes) {
        this.startTimes = startTimes;
    }
    
    public String[] getEndTimes() {
        return endTimes;
    }

    public void setEndTimes(String[] endTimes) {
        this.endTimes = endTimes;
    }
}
