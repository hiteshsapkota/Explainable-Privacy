package com.expriv.config;

import com.expriv.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.expriv.service.UserService;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserService userService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .antMatchers("/", "/home", "/registration**", "/register_success","/forgot-password","/reset-password","/js/**", "/css/**", "/img/**", "/webjars/**")
            .permitAll().anyRequest().authenticated().and().formLogin().loginPage("/login")
            .defaultSuccessUrl("/index").permitAll().and().logout().invalidateHttpSession(true)
            .clearAuthentication(true).logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login?logout").permitAll();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
    auth.setUserDetailsService(userService);
    auth.setPasswordEncoder(passwordEncoder());
    return auth;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authenticationProvider());
  }

  @Configuration
  public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

      ConfigurationService configurationService = new ConfigurationService();

      configurationService.setParams();
      registry
              .addResourceHandler("/images/**")
              .addResourceLocations("file:resources/", "file:uploads/", "file:"+configurationService.getPython_base_dir()+"images/")
              .setCachePeriod(0);
    }
  }
}
