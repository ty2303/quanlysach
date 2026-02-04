package vn.hutech.trandinhty_2280618597.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @org.springframework.beans.factory.annotation.Autowired
        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        @org.springframework.beans.factory.annotation.Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authz -> authz
                                                // Public access
                                                .requestMatchers("/", "/register", "/register-admin", "/login",
                                                                "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/books", "/books/search").permitAll()
                                                .requestMatchers("/api/books", "/api/books/**").permitAll()
                                                // MoMo callback endpoints - must be public
                                                .requestMatchers("/checkout/momo/return", "/checkout/momo/ipn")
                                                .permitAll()
                                                // Admin only - quản lý sách và danh mục
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/books/new", "/books/edit/**", "/books/delete/**",
                                                                "/books/update")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/categories/new", "/categories/edit/**",
                                                                "/categories/delete/**", "/categories/update")
                                                .hasRole("ADMIN")
                                                // User only - giỏ hàng, checkout và đơn hàng (ADMIN không được truy
                                                // cập)
                                                .requestMatchers("/cart/**", "/checkout/**", "/orders/**")
                                                .hasRole("USER")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(customAuthenticationSuccessHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .permitAll())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**", "/checkout/momo/ipn"));

                return http.build();
        }
}
