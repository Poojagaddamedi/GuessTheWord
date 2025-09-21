package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusResponse {
    private Long gameId;
    private String targetWord; // Only show if game is completed
    private Integer remainingGuesses;
    private boolean gameCompleted;
    private Boolean isWon;
    private String message;
    private List<GuessResponse.PreviousGuess> previousGuesses;
}