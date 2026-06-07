package com.yummi.seven.config;

import com.yummi.seven.filter.JWTFilter;
import com.yummi.seven.filter.LoginFilter;
import com.yummi.seven.handler.LoginSuccessHandler;
import com.yummi.seven.util.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final LoginSuccessHandler loginSuccessHandler;
    private final JWTUtil jwtUtil;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            LoginSuccessHandler loginSuccessHandler,
            JWTUtil jwtUtil
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        // 수많은 필터 중 CSRF 필터를 disable 시킴
        http
                .csrf(csrf -> csrf.disable());

        // 기본 로그인
        http
                .formLogin(login -> login.disable());

        // 경로별 인가
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/join").permitAll()
                        .requestMatchers("/api/v1/").permitAll()
                        .requestMatchers("/api/v1/user").hasRole("USER")
                        .anyRequest().hasRole("ADMIN")
                );

        // 커스텀 필터 추가
        http
                .addFilterAfter(new JWTFilter(jwtUtil), SecurityContextHolderFilter.class);

        http
                .addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration), loginSuccessHandler), UsernamePasswordAuthenticationFilter.class);

        // 세션 설정 STATELESS
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

}
