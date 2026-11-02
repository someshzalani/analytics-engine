package com.example.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScoreSubmittedEvent {
    private final String username;
    private final Long score;
}