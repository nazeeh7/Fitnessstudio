package kickstart.mitarbeiter;

import jakarta.persistence.*;

@Entity
@Table(name = "paychecks")
public class Paycheck {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private double salary;
    private double overtime;
    private double bonuses;
    // Weitere relevante Attribute
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    public Paycheck() {
    }

    public Paycheck(double salary, double overtime, double bonuses) {
        this.salary = salary;
        this.overtime = overtime;
        this.bonuses = bonuses;
    }

    public Long getId() {
        return id;
    }

    public double getSalary() {
        return salary;
    }
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    public void setSalary(double salary) {
        this.salary = salary;
    }

    public double getOvertime() {
        return overtime;
    }

    public void setOvertime(double overtime) {
        this.overtime = overtime;
    }

    public double getBonuses() {
        return bonuses;
    }

    public void setBonuses(double bonuses) {
        this.bonuses = bonuses;
    }
}
