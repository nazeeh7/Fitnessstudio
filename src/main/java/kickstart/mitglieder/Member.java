package kickstart.mitglieder;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "address")
    private String address;

    @Column(name = "birthdate")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    
    @Column(name = "gender")
    private String gender;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;
    
    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;
    
    private String role;
    
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "contract_id", referencedColumnName = "id")
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private MembershipContract contract;

    @OneToOne(mappedBy = "member", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TrainingPlan trainingPlan;



    // Konstruktoren
    public Member() {}

    public Member(String firstName, String lastName, String address, LocalDate birthdate, String gender, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthdate = birthdate;
        this.gender = gender;
        this.phone = phone;
        this.email = email;

    }


 // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public MembershipContract getContract() {
        return this.contract;
    }
    public void setContract(MembershipContract contract) {
        this.contract = contract;
        if(contract != null) {
            contract.setMember(this);
        }
    }

    public TrainingPlan getTrainingPlan() {
        return this.trainingPlan;
    }

    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
        if (trainingPlan != null) {
            trainingPlan.setMember(this);
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
