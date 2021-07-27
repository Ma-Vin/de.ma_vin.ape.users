package de.ma_vin.ape.users.security;

import de.ma_vin.ape.users.security.filter.ClientCheckFilter;
import de.ma_vin.ape.users.security.service.UserExtDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private UserExtDetailsService userExtDetailsService;

    @Autowired
    private ClientCheckFilter clientCheckFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userExtDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/oauth/authorize").authenticated().and().formLogin();
       // http.authorizeRequests().anyRequest().permitAll().and().cors().and().csrf().disable().headers().frameOptions().sameOrigin();
    }

    @Bean
    public FilterRegistrationBean<ClientCheckFilter> clientCheckFilterRegistrationBean() {
        FilterRegistrationBean<ClientCheckFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(clientCheckFilter);
        registrationBean.addUrlPatterns("/auth/oauth/token");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
