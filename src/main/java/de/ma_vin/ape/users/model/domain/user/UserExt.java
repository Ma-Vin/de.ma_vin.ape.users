package de.ma_vin.ape.users.model.domain.user;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.utils.properties.SystemProperties;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@ExtendingDomain
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("java:S2160")
public class UserExt extends User implements UserDetails {

    private Set<SimpleGrantedAuthority> authorities;

    public UserExt(String firstName, String lastName, Role role) {
        super();
        setFirstName(firstName);
        setLastName(lastName);
        setRole(role);
        initAuthorities();
    }

    /**
     * Sets a raw password encoded
     *
     * @param passwordEncoder encoder to transform given raw password
     * @param rawPassword     the password to set encoded
     */
    public void setRawPassword(PasswordEncoder passwordEncoder, String rawPassword) {
        setPassword(passwordEncoder.encode(rawPassword));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return getIdentification();
    }

    @Override
    public boolean isAccountNonExpired() {
        LocalDateTime now = SystemProperties.getSystemDateTime();
        return (getValidFrom() == null || now.isAfter(getValidFrom()) || now.isEqual(getValidFrom()))
                && (getValidTo() == null || now.isBefore(getValidTo()) || now.isEqual(getValidTo()));
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isAccountNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return !Role.BLOCKED.equals(getRole());
    }

    private void initAuthorities() {
        authorities = new HashSet<>();
        if (isGlobalAdmin()) {
            authorities.add(new SimpleGrantedAuthority("GlobalAdmin"));
        }
        if (getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(getRole().name()));
        }
    }
}
