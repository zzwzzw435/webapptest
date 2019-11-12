package com.wonderfulteam.dao;

import com.wonderfulteam.bean.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Qixiang Zhou on 2019-09-28 23:33
 */

// Amazing: This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
}
