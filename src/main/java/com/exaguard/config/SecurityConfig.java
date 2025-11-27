package com.exaguard.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in this PoC
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/index.html"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/auth/**"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/css/**"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/js/**"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/models/**"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/images/**"),
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/favicon.ico")
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/index.html")
                .loginProcessingUrl("/login")
                .successHandler(successHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/index.html")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        org.springframework.security.core.userdetails.UserDetails proctor = org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder()
            .username("admin@visilpro.com")
            .password("asdf@IMN213")
            .roles("PROCTOR")
            .build();

        org.springframework.security.core.userdetails.UserDetails student = org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder()
            .username("student@visilpro.com")
            .password("asdf@IMN213")
            .roles("STUDENT")
            .build();

        return new org.springframework.security.provisioning.InMemoryUserDetailsManager(proctor, student);
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROCTOR"))) {
                response.sendRedirect("/dashboard.html");
            } else {
                response.sendRedirect("/exam.html");
            }
        };
    }
}
