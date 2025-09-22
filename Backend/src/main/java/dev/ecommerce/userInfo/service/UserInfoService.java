package dev.ecommerce.userInfo.service;

import dev.ecommerce.auth.jwt.JwtService;
import dev.ecommerce.exceptionHandler.ResourceNotFoundException;
import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.user.entity.User;
import dev.ecommerce.user.repository.UserRepository;
import dev.ecommerce.userInfo.DTO.UserAddress;
import dev.ecommerce.userInfo.DTO.UserBasicInfo;
import dev.ecommerce.userInfo.entity.UserUsageInfo;
import dev.ecommerce.userInfo.repository.UserUsageInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserItemService userItemService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserUsageInfoRepository userUsageInfoRepository;

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + userId)
        );
    }

    @Transactional(readOnly = true)
    public UserBasicInfo getUserBasicInfo(Long userId) {
        User user = findUserById(userId);
        return new UserBasicInfo(
                user.getFirstname(),
                user.getLastname(),
                user.getUsername(),
                null, null, null
        );
    }

    @Transactional
    public String updateFirstNameAndLastName(Long userId, UserBasicInfo userBasicInfo) {
        User user = findUserById(userId);

        boolean updated = false;
        if (user.getFirstname() != null && !user.getFirstname().isBlank()) {
            user.setFirstname(userBasicInfo.getFirstname());
            updated = true;
        }
        if (user.getLastname() != null && !user.getLastname().isBlank()) {
            user.setLastname(userBasicInfo.getLastname());
            updated = true;
        }
        if (!updated) {
            throw new IllegalArgumentException("No name provided");
        }
        return userRepository.save(user).getFirstname() + " " + user.getLastname();
    }

    @Transactional
    public Pair<ResponseCookie, String> updateEmail(Long userId, UserBasicInfo userBasicInfo) {
        if (userBasicInfo.getEmail() == null || userBasicInfo.getEmail().isBlank()) {
            throw new IllegalArgumentException("No email provided");
        }
        User user = findUserById(userId);
        user.setUsername(userBasicInfo.getEmail());
        User savedUser = userRepository.save(user);
        return Pair.of(jwtService.makeAuthenticateCookie(new SecurityUser(savedUser)), savedUser.getUsername());
    }

    @Transactional
    public String updatePassword(Long userId, UserBasicInfo userBasicInfo) {
        if (!userBasicInfo.getNewPass().equals(userBasicInfo.getConfirmPass())) {
            throw new BadCredentialsException("New Password Mismatch");
        }
        User user = findUserById(userId);
        if (!passwordEncoder.matches(userBasicInfo.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(userBasicInfo.getNewPass()));
        return userRepository.save(user).getFirstname();
    }

    public UserAddress getAddress(Long userId) {
        return userItemService.findUserInfoByUserId(userId).getUserAddressDetails();
    }

    @Transactional
    public UserAddress updateAddress(Long userId, UserAddress userAddress) {
        UserUsageInfo userInfo = userItemService.findUserInfoByUserId(userId);
        userInfo.setAddress(
                userAddress.street(),
                userAddress.city(),
                userAddress.state(),
                userAddress.zipcode(),
                userAddress.country()
        );
        return userUsageInfoRepository.save(userInfo).getUserAddressDetails();
    }

}
