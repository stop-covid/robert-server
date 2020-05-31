package fr.gouv.stopc.robert.config.util;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/**
 * WebSecurity configuration class enabling keycloak check<br>
 * - JSR-250 enables to use the annotation RolesAllowed on the endpoints methods
 * - Enabled only if keycloak enabled
 * 
 * @author plant-stopcovid
 * @version 0.0.1-SNAPSHOT
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@ConditionalOnProperty(value = "keycloak.enabled", havingValue = "true")
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	/**
	 * Configuration of the Authentication to use the KeyCloak provider
	 * 
	 * @param auth the Spring authentication manager builder to configure
	 * @since 0.0.1-SNAPSHOT
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	/**
	 * Bean used to read the configuration from application.yml instead of
	 * keycloak.json
	 * 
	 * @return the keycloak conf resolver to use
	 * @since 0.0.1-SNAPSHOT
	 */
	@Bean
	public KeycloakConfigResolver KeycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	/**
	 * {@inheritDoc}
	 */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.csrf().disable().authorizeRequests().anyRequest().permitAll();
	}

}