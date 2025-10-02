package iranga.mg.social.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import iranga.mg.social.auth.ChatUserDetailsService;
import iranga.mg.social.security.JwtRequestFilter;

@Configuration
public class WebSecurityConfig {

	@Autowired
	private JwtRequestFilter jwtAuthenticationFilter;

	@Autowired
	private ChatUserDetailsService userDetailsService;

	@Bean
	// @Order(1)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webrtc-signaling/**")
			.csrf(csrf -> csrf.disable())
			.cors().and()
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // <== Autoriser prévol CORS
				.requestMatchers("/api/auth/**").permitAll()
				.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
				.requestMatchers("/api/files/download/**").permitAll()
				.requestMatchers("/webrtc-signaling/**").permitAll()
				.anyRequest().authenticated()
			);

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}


	// @Bean
	// @Order(2)
	// public SecurityFilterChain mvcSecurityFilterChain(HttpSecurity http) throws Exception {
	// 	http.securityMatcher("/**")
	// 			.csrf(csrf -> csrf.disable()) // Vous pouvez activer CSRF si nécessaire pour les vues
	// 			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
	// 			.authorizeHttpRequests(auth -> auth
	// 					.requestMatchers("/", "/sign-in", "/sign-up", "/disconnect").permitAll()
	// 					.requestMatchers("/assets/**", "/icons/**").permitAll()
	// 					.requestMatchers("/ws/**").permitAll()
	// 					.anyRequest().authenticated()
	// 			)
	// 			.formLogin(form -> form
	// 					.loginPage("/sign-in")
	// 					.loginProcessingUrl("/mvc-login")
	// 					.defaultSuccessUrl("/dashboard", true)
	// 					.failureUrl("/sign-in?error=true")
	// 					.permitAll()
	// 			)
	// 			.logout(logout -> logout
	// 					.logoutUrl("/logout")
	// 					.logoutSuccessUrl("/sign-in?logout=true")
	// 					.invalidateHttpSession(true)
	// 					.deleteCookies("JSESSIONID")
	// 					.permitAll()
	// 			);

	// 	return http.build();
	// }

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}
