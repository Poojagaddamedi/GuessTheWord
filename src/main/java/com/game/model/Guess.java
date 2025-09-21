package com.game.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guesses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "guessed_word", nullable = false, length = 5)
    private String guessedWord;

    @Column(name = "guess_number", nullable = false)
    private Integer guessNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "feedback", length = 5)
    private String feedback; // G=Green, O=Orange, R=Grey for each letter position
}