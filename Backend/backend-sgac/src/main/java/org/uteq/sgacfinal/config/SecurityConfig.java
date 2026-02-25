package org.uteq.sgacfinal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.security.JwtAuthenticationFilter;
import org.uteq.sgacfinal.security.JwtService;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

//    @Bean
//    @Transactional
//    public UserDetailsService userDetailsService() {
//        return username -> usuarioRepository.findByNombreUsuarioWithRolesAndTipoRol(username)
//                .map(UsuarioPrincipal::new)
//                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
//    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();

                UserDetails user = userDetailsService.loadUserByUsername(username);

                if (!passwordEncoder().matches(password, user.getPassword())) {
                    throw new BadCredentialsException("Contraseña incorrecta");
                }
                if (!user.isEnabled()) {
                    throw new DisabledException("La cuenta está inactiva");
                }
                return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/registro-estudiante").permitAll()
                        .requestMatchers("/api/auth/registro-admin", "/api/auth/registro-decano", "/api/auth/registro-coordinador").hasAuthority("ADMINISTRADOR")
                        .requestMatchers("/api/auth/registro-docente", "/api/auth/promover-estudiante").hasAnyAuthority("ADMINISTRADOR", "COORDINADOR")
                        .requestMatchers("/api/convocatorias/crear", "/api/convocatorias/editar/**").hasAnyAuthority("DOCENTE", "COORDINADOR")
                        .requestMatchers("/api/permisos/consultar", "/api/tipos-rol/resumen-permisos", "/api/permisos/gestionar", "/api/permisos/esquemas", "/api/permisos/tipos-objeto", "/api/permisos/elementos", "/api/permisos/privilegios/**").hasAnyAuthority("ADMINISTRADOR")
                        .requestMatchers("/api/convocatorias/**").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}