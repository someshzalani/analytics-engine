package com.example.analytics;

import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService service;

    public LeaderboardController(LeaderboardService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public void submitScore(@RequestParam String username, @RequestParam Long score) {
        service.updateScore(username, score);
    }

    @GetMapping("/top")
    public Set<String> getTopPlayers(@RequestParam(defaultValue = "10") int limit) {
        return service.getTopPlayers(limit);
    }
}