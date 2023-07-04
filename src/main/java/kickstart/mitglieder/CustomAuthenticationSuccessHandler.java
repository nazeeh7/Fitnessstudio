package kickstart.mitglieder;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isUser = authorities.contains(new SimpleGrantedAuthority("ROLE_USER"));
        boolean isEmployee = authorities.contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        boolean isTrainer = authorities.contains(new SimpleGrantedAuthority("ROLE_TRAINER")); // Überprüfen, ob der Benutzer die Rolle "TRAINER" hat
        boolean isBoss = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (isUser) {
            getRedirectStrategy().sendRedirect(request, response, "/ansicht_member_dashboard");
        } else if (isEmployee) {
            String redirectUrl = "/employeeView/" + username;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else if (isTrainer) { // Wenn der Benutzer die Rolle "TRAINER" hat
        	 String redirectUrl = "/employeeView/" + username;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl); // Weiterleitung zur Trainer-Seite
        } else if (isBoss) {
            getRedirectStrategy().sendRedirect(request, response, "/welcome");
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
        boolean isTrainer1 = authorities.contains(new SimpleGrantedAuthority("ROLE_TRAINER"));

        if (isTrainer1) {
            System.out.println("Der Trainer hat die Rolle TRAINER.");
        } else {
            System.out.println("Der Trainer hat nicht die Rolle TRAINER.");
        }


    }
}
