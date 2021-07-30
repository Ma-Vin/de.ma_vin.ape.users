package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.properties.AuthClients;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class ClientDetailService implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    AuthClients authClients;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authClients.getClients().stream()
                .filter(c -> c.getClientId().equals(username))
                .map(c -> new ClientDetails(c.getClientId(), passwordEncoder.encode(c.getSecret())))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException(String.format("The client with identification %s does not exist", username)));
    }

    @Data
    @AllArgsConstructor
    private static class ClientDetails implements UserDetails {

        private String username;
        private String password;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            Set<SimpleGrantedAuthority> result = new HashSet<>();
            result.add(new SimpleGrantedAuthority("client"));
            return result;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
