package dev.ecommerce.user.service;


import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Will be used to return our customer security user that implement UserDetails to store our User entity
 */

@Service
public class JpaUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public Optional<SecurityUser> findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(SecurityUser::new);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
