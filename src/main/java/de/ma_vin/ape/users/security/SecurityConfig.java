package de.ma_vin.ape.users.security;

import de.ma_vin.ape.users.properties.AuthClients;
import de.ma_vin.ape.users.security.filter.ClientCheckFilter;
import de.ma_vin.ape.users.security.service.ClientDetailService;
import de.ma_vin.ape.users.security.service.UserExtDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String TOKEN_PATTERN = "/oauth/token";
    public static final String AUTHORIZE_PATTERN = "/oauth/authorize";
    public static final String INTROSPECTION_PATTERN = "/oauth/introspection";
    public static final String CONSOLE_PATTERN = "/console/**";
    public static final String OAUTH_SECURED_REGEX = "\\/(group|user|admin)\\/.*";

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-uri}")
    private String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaque.introspection-client-secret}")
    private String clientSecret;

    @Value("${spring.h2.console.enabled}")
    private boolean h2ConsoleEnabled;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<ClientCheckFilter> clientCheckFilterRegistrationBean(ClientCheckFilter clientCheckFilter) {
        FilterRegistrationBean<ClientCheckFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(clientCheckFilter);
        registrationBean.addUrlPatterns(TOKEN_PATTERN);
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AuthClients authClients) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(authClients.getClients().stream().map(AuthClients.Client::getUrl).toList());
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authManager(BCryptPasswordEncoder passwordEncoder
            , UserExtDetailsService userDetailsService, ClientDetailService clientDetailService) {

        DaoAuthenticationProvider clientAuthProvider = new DaoAuthenticationProvider();
        clientAuthProvider.setPasswordEncoder(passwordEncoder);
        clientAuthProvider.setUserDetailsService(clientDetailService);

        DaoAuthenticationProvider userAuthProvider = new DaoAuthenticationProvider();
        userAuthProvider.setPasswordEncoder(passwordEncoder);
        userAuthProvider.setUserDetailsService(userDetailsService);

        return new ProviderManager(clientAuthProvider, userAuthProvider);
    }


    @Bean
    @Order(1)
    public SecurityFilterChain oAuthTokenFilterChain(HttpSecurity http, AuthClients authClients) throws Exception {
        if (authClients.isTokenWithClientSecret()) {
            http.antMatcher(TOKEN_PATTERN).authorizeRequests().anyRequest().authenticated()
                    .and().httpBasic();
        } else {
            http.antMatcher(TOKEN_PATTERN).authorizeRequests().anyRequest().permitAll();
        }
        http.cors().and().csrf().disable();

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain introspectionFilterChain(HttpSecurity http) throws Exception {
        http.antMatcher(INTROSPECTION_PATTERN).authorizeRequests().anyRequest().authenticated()
                .and().cors()
                .and().httpBasic()
                .and().csrf().disable();

        return http.build();
    }


    @Bean
    @Order(3)
    public SecurityFilterChain oAuthResourceFilterChain(HttpSecurity http) throws Exception {
        http.regexMatcher(OAUTH_SECURED_REGEX).authorizeRequests().anyRequest().authenticated().and()
                .oauth2ResourceServer(
                        oauth2 -> oauth2.opaqueToken(token -> token.introspectionUri(this.introspectionUri)
                                .introspectionClientCredentials(this.clientId, this.clientSecret)
                        )
                )
                .cors();

        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
        if (h2ConsoleEnabled) {
            http.antMatcher(CONSOLE_PATTERN).authorizeRequests().anyRequest().permitAll()
                    .and().csrf().disable()
                    .headers().frameOptions().disable();
        } else {
            http.antMatcher(CONSOLE_PATTERN).authorizeRequests().anyRequest().denyAll();
        }
        return http.build();
    }

    @Bean
    @Order(5)
    public SecurityFilterChain oAuthAuthorizeFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(AUTHORIZE_PATTERN).authenticated()
                .and().cors()
                .and().formLogin();

        return http.build();
    }

}
