package dev.ecommerce.userInfo.repository;

import dev.ecommerce.userInfo.entity.UserUsageInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUsageInfoRepository extends JpaRepository<UserUsageInfo, Long> {

}
