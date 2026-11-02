package com.example.analytics;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ScoreEventListener {

    @Async
    @EventListener
    public void handleScoreSubmitted(ScoreSubmittedEvent event) {
        // Simulate heavy processing (e.g., writing analytics logs, computing badges)
        try {
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[Async Worker Thread: " + Thread.currentThread().getName() + 
                           "] Processed background task for player: " + event.getUsername());
    }
}