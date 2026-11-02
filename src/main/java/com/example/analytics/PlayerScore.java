package com.example.analytics;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_scores", indexes = @Index(name = "idx_score", columnList = "score"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScore {

    @Id
    private String username;

    private Long score;

    @Version
    private Long version; // Optimistic Locking field to handle concurrent updates
}