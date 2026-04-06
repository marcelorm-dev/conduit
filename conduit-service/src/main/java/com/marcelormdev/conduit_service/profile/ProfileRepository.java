package com.marcelormdev.conduit_service.profile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserUsername(String username);

    Optional<Profile> findByUserEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM profile_follows", nativeQuery = true)
    void deleteAllFollows();

}
