package dev.ecommerce.user.repository;

import dev.ecommerce.user.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCartRepository extends JpaRepository<UserItem, Long> {
}
