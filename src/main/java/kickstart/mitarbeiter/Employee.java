package kickstart.mitarbeiter;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private int age;
    private String gender;
    private String address;
    @OneToMany(mappedBy = "employee")
    private List<Schedule> schedules;

    @ElementCollection
    @CollectionTable(name = "qualifications")
    @Column(name = "qualification")
    private List<String> qualifications;

    private double salary;
    private int day;
    private int month;
    private int year;
    private String username;
    private String password;
    @ElementCollection
    @CollectionTable(name = "employee_roles")
    @Column(name = "role")
    private List<String> role;
    public Employee() {
    }

    public Employee(String name, int age, String gender, String address, List<String> qualifications, double salary,
            int day, int month, int year,  String username, String password) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.qualifications = qualifications;
        this.salary = salary;
        this.day = day;
        this.month = month;
        this.year = year;
        this.username=username;
        this.password=password;
    }

    public String getName() {
        return name;
    }
    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getQualifications() {
        return qualifications;
    }
    public String getQualificationsAsString() {
        return String.join(", ", qualifications);
    }

    public void setQualifications(List<String> qualifications) {
        this.qualifications = qualifications;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }
}