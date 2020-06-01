package fr.gouv.stopc.robert.cockpit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@SpringBootApplication
public class RobertCockpitApplication {

	public static void main(String[] args) {
		SpringApplication.run(RobertCockpitApplication.class, args);
	}

}
