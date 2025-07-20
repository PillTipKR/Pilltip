package com.oauth2.User.Hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HospitalAccessTokenRepository extends JpaRepository<HospitalAccessToken, Long> {
    Optional<HospitalAccessToken> findByHospitalCode(String hospitalCode);
    Optional<HospitalAccessToken> findByAccessToken(String accessToken);
    Optional<HospitalAccessToken> findByHospitalCodeAndTokenDate(String hospitalCode, LocalDate tokenDate);
    void deleteByHospitalCode(String hospitalCode);
} 