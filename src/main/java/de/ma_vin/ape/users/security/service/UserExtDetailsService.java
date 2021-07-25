package de.ma_vin.ape.users.security.service;

import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserExtDetailsService implements UserDetailsService {

    @Autowired
    UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userIdentification) throws UsernameNotFoundException {
        return (UserExt) userService.findUser(userIdentification).orElseThrow(
                () -> new UsernameNotFoundException(String.format("The User with identification %s does not exist", userIdentification))
        );
    }
}
