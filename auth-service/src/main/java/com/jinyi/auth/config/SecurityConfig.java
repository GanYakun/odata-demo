package com.jinyi.auth.config;

import com.jinyi.auth.service.UserDetailsServiceImpl;
import com.jinyi.auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Spring Security Configuration
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Public endpoints
                .antMatchers("/auth/login", "/auth/refresh", "/auth/health").permitAll()
                .antMatchers("/test/**").permitAll() // Test endpoints
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                // All other requests need authentication
                .anyRequest().authenticated()
            .and()
            .headers().frameOptions().disable() // For H2 console
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    /**
     * JWT Authentication Filter
     */
    public class JwtAuthenticationFilter extends OncePerRequestFilter {
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                FilterChain filterChain) throws ServletException, IOException {
            
            String token = extractToken(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // Load user details
                        var userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // Create authentication token
                        var authToken = new JwtAuthenticationToken(userDetails, token, userDetails.getAuthorities());
                        authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request));
                        
                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("JWT authentication successful for user: {}", username);
                    }
                } catch (Exception e) {
                    log.error("JWT authentication failed", e);
                    SecurityContextHolder.clearContext();
                }
            }
            
            filterChain.doFilter(request, response);
        }
        
        private String extractToken(HttpServletRequest request) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        }
    }
    
    /**
     * Custom JWT Authentication Token
     */
    public static class JwtAuthenticationToken implements Authentication {
        
        private final Object principal;
        private final String token;
        private final java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;
        private Object details;
        private boolean authenticated = true;
        
        public JwtAuthenticationToken(Object principal, String token, 
                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
            this.principal = principal;
            this.token = token;
            this.authorities = authorities;
        }
        
        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public Object getCredentials() {
            return token;
        }
        
        @Override
        public Object getDetails() {
            return details;
        }
        
        public void setDetails(Object details) {
            this.details = details;
        }
        
        @Override
        public Object getPrincipal() {
            return principal;
        }
        
        @Override
        public boolean isAuthenticated() {
            return authenticated;
        }
        
        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            this.authenticated = isAuthenticated;
        }
        
        @Override
        public String getName() {
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }
            return principal.toString();
        }
    }
}