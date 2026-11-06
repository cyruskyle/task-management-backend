package com.cyruskyle.taskmanagement.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			// Credentials are sent explicitly per request, so no cookie-based CSRF surface exists.
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(basic -> basic.realmName("Task Management API"))
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/health/**").permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
				.requestMatchers("/api/**").hasRole("USER")
				.anyRequest().denyAll()
			);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(
			@Value("${app.auth.username}") String username,
			@Value("${app.auth.password}") String password,
			PasswordEncoder passwordEncoder) {
		if (username.isBlank() || password.isBlank()) {
			throw new IllegalStateException("APP_AUTH_USERNAME and APP_AUTH_PASSWORD must be set");
		}

		var user = User.withUsername(username)
			.password(passwordEncoder.encode(password))
			.roles("USER")
			.build();

		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origin:}") String allowedOrigin) {
		var configuration = new CorsConfiguration();
		if (!allowedOrigin.isBlank()) {
			configuration.setAllowedOrigins(List.of(allowedOrigin));
		}
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setMaxAge(3600L);

		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

}
