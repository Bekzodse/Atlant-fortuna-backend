package sayyeed.dev.atlantbackend.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SpringSecurityConfig(CustomUserDetailsService customUserDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, JwtAuthenticationFilter jwtAuthenticationFilter){
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    public static String[] openApiList = {
            "/api/v1/auth/login",
            "/api/v1/team/public/**",
            "/api/v1/contact/public",
            "/api/v1/products/public/**",
            "/api/v1/carousel/public/**",
            "/api/v1/gallery/public/**",
            "/api/v1/reels/public/**",
            "/api/v1/licenses/public/**",
            "attach/download/*",
            "/api/v1/ai/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };


    @Bean
    public AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry
                            .requestMatchers(openApiList).permitAll()
                            .requestMatchers("/api/v1/products/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/attach/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/carousel/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/team/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/contact/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/gallery/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/reels/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/licenses/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/telegram/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/auth/register").hasRole("ADMIN")
                            .anyRequest()
                            .authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.getWriter().write("Authentication required: " + authException.getMessage());
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.getWriter().write("Access denied: " + accessDeniedException.getMessage());
                                })
                );

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(httpSecurityCorsConfigurer -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
            configuration.setAllowedMethods(Arrays.asList("*"));
            configuration.setAllowedHeaders(Arrays.asList("*"));

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            httpSecurityCorsConfigurer.configurationSource(source);
        });
        return http.build();
    }

}
