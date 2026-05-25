package com.voltstore.repository;

import com.voltstore.model.CartItem;
import com.voltstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    
    @Modifying
    @Query("delete from CartItem c where c.user = :user")
    void deleteByUser(@Param("user") User user);
}
