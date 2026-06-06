package com.filemanager.fm.Repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.filemanager.fm.Entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
