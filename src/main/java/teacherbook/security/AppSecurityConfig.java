package teacherbook.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import teacherbook.security.handlers.SecurityErrorHandler;
import teacherbook.security.service.AppUserDetailsService;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppSecurityConfig(PasswordEncoder pE) {
        this.passwordEncoder = pE;
    }

    @Autowired
    private AppUserDetailsService postgreUserService;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Autowired
    public void confGlobalAuthManager(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService uDS = postgreUserService;
        auth
                .userDetailsService(uDS)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/home", "/images/**", "/login", "/register", "/err", "/pswd_reset/**", "/pswd_reset_form", "/registrationConfirm").permitAll()
                .anyRequest()
                    .authenticated()
                    .and()
                    .formLogin()
                        .loginPage("/login")
                        .defaultSuccessUrl("/logged-in")
                        .usernameParameter("username")
                        .passwordParameter("password")
                    .and()
                        .rememberMe()
                        .tokenValiditySeconds((int)TimeUnit.DAYS.toSeconds(30))
                    .and()
                    .logout()
                        .logoutUrl("/logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .logoutSuccessUrl("/")
                    .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/logout")
                        .maximumSessions(3)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                    .and()
                        .sessionAuthenticationFailureHandler(new SecurityErrorHandler());
    }
}