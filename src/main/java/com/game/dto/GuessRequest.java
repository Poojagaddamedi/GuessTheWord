package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessRequest {
    private Long gameId;
    private String guessedWord; // 5-letter word in uppercase

    // Alternative getter for backward compatibility
    public String getGuess() {
        return this.guessedWord;
    }
}