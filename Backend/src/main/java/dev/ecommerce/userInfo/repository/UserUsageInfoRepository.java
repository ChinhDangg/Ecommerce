package dev.ecommerce.userInfo.repository;

import dev.ecommerce.userInfo.entity.UserUsageInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserUsageInfoRepository extends JpaRepository<UserUsageInfo, Long> {

    Optional<UserUsageInfo> findByUserId(Long userId);
}
