package com.app.cabbie.repository;

import com.app.cabbie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User ,Long> {

    Optional<User> findByEmail(String email);
}
