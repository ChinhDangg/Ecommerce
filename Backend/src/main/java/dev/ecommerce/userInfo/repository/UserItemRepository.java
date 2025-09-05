package dev.ecommerce.userInfo.repository;

import dev.ecommerce.userInfo.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {
}
