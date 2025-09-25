package dev.ecommerce.user.service;


import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.repository.UserRepository;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import dev.ecommerce.userInfo.repository.UserUsageInfoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Will be used to return our customer security user that implement UserDetails to store our User entity
 */

@Service
public class JpaUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserUsageInfoRepository userInfoRepository;

    public JpaUserDetailService(UserRepository userRepository, UserUsageInfoRepository suerInfoRepository) {
        this.userRepository = userRepository;
        this.userInfoRepository = suerInfoRepository;
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
        UserUsageInfo userInfo = new UserUsageInfo(user, Instant.now(), user.getFullName());
        userInfoRepository.save(userInfo);
        return userRepository.save(user);
    }
}
