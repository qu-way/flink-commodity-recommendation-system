package com.ly.recommend_backend.dao;

import com.ly.recommend_backend.entity.ProductEntity;
import com.ly.recommend_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserInterface extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    User getUserById(Integer id);
    User getUserByName(String name);
    User save(User user);
}
