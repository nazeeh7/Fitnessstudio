//package kickstart.mitglieder;
//
//import org.salespointframework.useraccount.UserAccountManagement;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserAccountManagement userAccountManager;
//    
//    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
//
//    @Autowired
//    public CustomUserDetailsService(UserAccountManagement userAccountManager) {
//        this.userAccountManager = userAccountManager;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        logger.info("loadUserByUsername() called with username: {}", username);
//
//        return userAccountManager.findByUsername(username)
//                .map(userAccount -> {
//                    List<GrantedAuthority> authorities = userAccount.getRoles().stream()
//                            .map(role -> new SimpleGrantedAuthority(role.toString()))
//                            .collect(Collectors.toList());
//
//                    logger.info("User found: {}, roles: {}", userAccount.getUsername(), authorities);
//
//                    return User.withUsername(userAccount.getUsername())
//                            .password(userAccount.getPassword().toString())
//                            .authorities(authorities)
//                            .build();
//                })
//                .orElseThrow(() -> {
//                    logger.error("Username not found: {}", username);
//                    return new UsernameNotFoundException("Username not found: " + username);
//                });
//    }
//
//}
