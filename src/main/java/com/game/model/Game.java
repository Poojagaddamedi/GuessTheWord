package com.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = true) // Allow null until first guess
    private Word word;

    @Column(name = "date_played", nullable = false)
    private LocalDate datePlayed;

    @Column(name = "is_won")
    private Boolean isWon;

    @Column(name = "remaining_guesses", nullable = false)
    private Integer remainingGuesses = 5;

    // Helper method to track game completion
    public boolean isCompleted() {
        return isWon != null;
    }
}