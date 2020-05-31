package fr.gouv.stopc.robert.config.util;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Class used to allow all requests for developpers<br>
 * - Enabled if keycloak is disabled
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@EnableWebSecurity
@ConditionalOnProperty(value = "keycloak.enabled", havingValue = "false")
public class DevSecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// On dev environement enable all requests without authentication
		http.csrf().disable().authorizeRequests().anyRequest().permitAll();
	}
}
