package com.example.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, String> {
    // Fetches top players directly from DB if cache misses
    List<PlayerScore> findTop10ByOrderByScoreDesc();

    @Modifying
    @Query("UPDATE PlayerScore p SET p.score = p.score + :points WHERE p.username = :username")
    int incrementScore(@Param("username") String username, @Param("points") Long points);
}