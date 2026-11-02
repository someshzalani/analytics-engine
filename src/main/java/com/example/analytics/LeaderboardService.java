package com.example.analytics;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class LeaderboardService {

    private final PlayerScoreRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private static final String LEADERBOARD_KEY = "leaderboard:global";

    public LeaderboardService(PlayerScoreRepository repository, 
        StringRedisTemplate redisTemplate, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
    }

@Transactional
public void updateScore(String username, Long pointsToAdd) {
    // 1. Try atomic update in PostgreSQL
        int updatedRows = repository.incrementScore(username, pointsToAdd);

        // 2. If the player doesn't exist yet, insert them safely
        if (updatedRows == 0) {
            try {
                PlayerScore player = new PlayerScore();
                player.setUsername(username);
                player.setScore(pointsToAdd);
                repository.save(player);
            } catch (Exception e) {
                // Fallback in case of concurrent insert race condition
                repository.incrementScore(username, pointsToAdd);
            }
        }

        // 3. Update Redis Sorted Set atomically using ZINCRBY equivalent
        redisTemplate.opsForZSet().incrementScore(LEADERBOARD_KEY, username, pointsToAdd);
        
        // Publish event for background processing
        eventPublisher.publishEvent(new ScoreSubmittedEvent(username, pointsToAdd));
}

    public Set<String> getTopPlayers(int limit) {
        // Fetch top elements directly from RAM
        return redisTemplate.opsForZSet().reverseRange(LEADERBOARD_KEY, 0, limit - 1);
    }
}