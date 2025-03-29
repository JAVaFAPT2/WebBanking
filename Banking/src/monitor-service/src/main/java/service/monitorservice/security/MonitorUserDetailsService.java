package service.monitorservice.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MonitorUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For demonstration purposes, we define a single hard-coded user.
        // In production, replace this with a service that loads the user details from your user store.
        if ("monitor".equalsIgnoreCase(username)) {
            // Note: "{noop}" is used to indicate that no password encoder is applied.
            return User.withUsername("monitor")
                    .password("{noop}monitorpassword")
                    .roles("MONITOR")
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
