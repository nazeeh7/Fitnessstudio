package kickstart.mitglieder;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Service
public class MemberService {

	   @Autowired
	    private UserAccountManagement userAccountManager;
    
    @Autowired
    private MemberRepository memberRepository;
    
    
    public boolean doesMemberIdExist(Long memberId) {
        return memberRepository.existsById(memberId);
    }

    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    public Member createMember(String firstName, String lastName, String address, LocalDate birthdate, String gender, String phone, String email, String username, String password) {
        Member member = new Member(firstName, lastName, address, birthdate, gender, phone, email);
//
        member.setUsername(username);
        member.setPassword(password);
        member.setRole("USER");

        memberRepository.save(member);
        return member;
    }
    public UserAccount createUserAccount(String username, String password) {
        return userAccountManager.create(username, Password.UnencryptedPassword.of(password), Role.of("USER"));
    }

  
    public Optional<Member> getMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }
//    public String generateUsername(String firstName, String lastName) {
//        String username = firstName.toLowerCase() + "." + lastName.toLowerCase();
//
//        // Check if this username already exists.
//        Optional<Member> existingMember = memberRepository.findByUsername(username);
//        if (existingMember.isPresent()) {
//            // If username exists, add a random number to it.
//            int rand = new Random().nextInt(100);
//            username += rand;
//        }
//        return username;
//    }

//    public String generatePassword() {
//        // This will generate a random six digit number as a password.
//        int rand = 100000 + new Random().nextInt(900000);
//        return Integer.toString(rand);
//    }
//    
//    public void printUserRole(String username) {
//        Optional<Member> memberOptional = memberRepository.findByUsername(username);
//        if(memberOptional.isPresent()) {
//            Member member = memberOptional.get();
//            System.out.println("Role of user: " + member.getRole());
//        } else {
//            System.out.println("User not found");
//        }
//    }
}

