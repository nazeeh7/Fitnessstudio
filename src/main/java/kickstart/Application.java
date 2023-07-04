package kickstart;

import org.salespointframework.EnableSalespoint;
import org.salespointframework.SalespointSecurityConfiguration;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import kickstart.mitarbeiter.CustomUserDetailsService;
import kickstart.mitarbeiter.EmployeeRepository;
import kickstart.mitglieder.CustomAuthenticationSuccessHandler;
import kickstart.mitglieder.MemberRepository;
import kickstart.mitglieder.MemberService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableSalespoint
@ComponentScan(basePackages = { "kickstart" })
@EnableWebSecurity
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Configuration
	@Order(200)
	static class WebSecurityConfiguration extends SalespointSecurityConfiguration {
		private final UserDetailsService customUserDetailsService;
		private final Logger log = LoggerFactory.getLogger(this.getClass());
		private final UserAccountManagement userAccountManager;

		@Autowired
		public WebSecurityConfiguration(
				@Qualifier("customUserDetailsService") UserDetailsService customUserDetailsService,
				UserAccountManagement userAccountManager) {
			this.customUserDetailsService = customUserDetailsService;
			this.userAccountManager = userAccountManager;
			log.info("Verwendeter UserDetailsService ist eine Instanz von: "
					+ customUserDetailsService.getClass().getSimpleName());
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable();

			http.authorizeRequests()
					.antMatchers("/", "/index", "/contact", "/about", "/images/**", "/login", "/resources/css/**",
							"/resources/js/**")
					.permitAll()
					.antMatchers("/ansicht_member_dashboard", "/member_personal_infos", "/ansicht_view_contract/{id}",
							"/ansicht_view_contract", "/trainingPlan/{id}", "/trainingPlan",
							"/cancellation-form", "/submitCancellation", "/cancellation-approval")
					.hasRole("USER")

					
					.antMatchers("/employeeView/{name}","/employeeView/{name}/personal-information", "/employeeView/{name}/request-holiday",
							"/employeeView/{name}/schedule", "/employees/{name}/request-holiday","/approved-requests","/rejected-requests")
					.hasAnyRole("EMPLOYEE", "TRAINER")

					.antMatchers("/training-plans/list-training-plans", "/training-plans/new", "/training-plans/{id}", "/training-plans/{id}/edit", "/training-plans/{id}/delete")
					.hasAnyRole("ADMIN", "TRAINER")
					

					.antMatchers("/verkaufsbereich/**", "/kundenArtikelliste", "/warenkorb",
							"/warenkorb/verkaufsbereich/{id}/hinzufuegen", "/warenkorb/verkaufsbereich/{id}/entfernen",
							"/bezahlung/verkaufsbereich/{id}/einzelArtikelkaufen", "/bezahlung",
							"/bezahlung/abschliessen", "/bezahlung/verkaufsbereich/{id}/sofortkauf",
							"/bezahlung/kaufen")
					.hasAnyRole("USER", "EMPLOYEE", "TRAINER")
					
					.antMatchers("/contract/{contractId}/download")
					.hasAnyRole("ADMIN", "USER")
					
					.antMatchers("/**", "/verkaufsbereich/**", "/artikelListe").hasRole("ADMIN") // Zugriff nur für
																									// Benutzer mit der
																									// Rolle "ADMIN"
					.and().formLogin().loginPage("/login").failureUrl("/login?error").successHandler(successHandler())
					.permitAll().and().logout().logoutUrl("/logout").logoutSuccessUrl("/");
		}

		@Bean
		public AuthenticationSuccessHandler successHandler() {
			return new CustomAuthenticationSuccessHandler();
		}

		@Override
		protected UserDetailsService userDetailsService() {
			return customUserDetailsService;
		}

		@Autowired
		public UserDetailsService customUserDetailsService(MemberRepository memberRepository,
				EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
			return new CustomUserDetailsService(memberRepository, employeeRepository, passwordEncoder,
					userAccountManager);
		}

	}

}