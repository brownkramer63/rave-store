package com.cydeo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityService securityService;
    private final AuthSuccessHandler authSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeRequests()
                    .antMatchers("/users/**").hasAnyAuthority("Root User", "Admin")
                    .antMatchers("/companies/**").hasAnyAuthority("Root User")
                    .antMatchers(
                            "/",
                            "/login",
                            "/about",
                            "/autoformprocessing",
                            "/customtotem",
                            "/vacation-scheduling",
                            "/vacation-scheduling/**",
                            "/fragments/**",
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/img/**",
                            "/assets/**",
                            "/webjars/**",
                            "/favicon.ico"
                    ).permitAll()
                    .anyRequest().authenticated()
                .and()
                    .formLogin()
                    .loginPage("/login")
                    .successHandler(authSuccessHandler)
                    .failureUrl("/login?error=true")
                    .permitAll()
                .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/login")
                .and()
                    .rememberMe()
                    .tokenValiditySeconds(86400)
                    .key("cataclysm")
                    .userDetailsService(securityService)
                .and()
                    .build();
    }
}
