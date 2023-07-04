package kickstart.verkaufsbereich.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.salespointframework.order.Cart;

@Configuration
public class MyAppConfig {

    @Bean
    public Cart cart() {
        return new Cart();
    }

    // andere Beans und Konfiguration...
}
