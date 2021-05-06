package de.ma_vin.ape.users.model.domain.user;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.util.layer.generator.annotations.model.ExtendingDomain;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@ExtendingDomain
public class UserExt extends User {

    public UserExt(String firstName, String lastName, Role role) {
        super();
        setFirstName(firstName);
        setLastName(lastName);
        setRole(role);
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
}
