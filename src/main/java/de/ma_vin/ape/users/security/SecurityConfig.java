package de.ma_vin.ape.users.security;

import de.ma_vin.ape.users.security.filter.ClientCheckFilter;
import de.ma_vin.ape.users.security.service.ClientDetailService;
import de.ma_vin.ape.users.security.service.UserExtDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private ClientCheckFilter clientCheckFilter;

    @Bean
    public FilterRegistrationBean<ClientCheckFilter> clientCheckFilterRegistrationBean() {
        FilterRegistrationBean<ClientCheckFilter> registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(clientCheckFilter);
        registrationBean.addUrlPatterns("/oauth/token");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Configuration
    @Order(4)
    public static class OAuthAuthorizeConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        private UserExtDetailsService userExtDetailsService;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userExtDetailsService);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().antMatchers("/oauth/authorize").authenticated().and().formLogin();
        }
    }

    @Configuration
    @Order(1)
    public static class OAuthTokenAndIntrospectionConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/oauth/token").authorizeRequests().anyRequest().permitAll().and().csrf().disable();
        }
    }

    @Configuration
    @Order(2)
    public static class OAuthIntrospectionConfig extends WebSecurityConfigurerAdapter {
        @Autowired
        private ClientDetailService clientDetailService;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(clientDetailService);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/oauth/introspection").authorizeRequests().anyRequest().authenticated().and().httpBasic().and().csrf().disable();
        }
    }


    @Configuration
    @Order(3)
    public static class OAuthResourceConfig extends WebSecurityConfigurerAdapter {

        @Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
        private String introspectionUri;

        @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
        private String clientId;

        @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
        private String clientSecret;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.regexMatcher("\\/(group|user)\\/.*").authorizeRequests().anyRequest().authenticated().and()
                    .oauth2ResourceServer(
                            oauth2 -> oauth2.opaqueToken(token -> token.introspectionUri(this.introspectionUri)
                                    .introspectionClientCredentials(this.clientId, this.clientSecret)
                            )
                    ).csrf().disable();
        }
    }
}
