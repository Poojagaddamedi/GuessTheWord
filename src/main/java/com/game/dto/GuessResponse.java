package com.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessResponse {
    @JsonProperty("isCorrect")
    private boolean correct; // Changed from isCorrect to correct

    @JsonProperty("gameCompleted")
    private boolean gameCompleted;

    @JsonProperty("won")
    private Boolean won; // true if won, false if lost, null if still playing

    @JsonProperty("message")
    private String message;

    @JsonProperty("gameId")
    private Long gameId;

    @JsonProperty("remainingGuesses")
    private Integer remainingGuesses;

    @JsonProperty("feedback")
    private String feedback; // G=Green, O=Orange, R=Grey for each letter

    @JsonProperty("targetWord")
    private String targetWord; // Only shown when game is completed

    @JsonProperty("currentGuess")
    private String currentGuess; // The word that was just guessed

    @JsonProperty("previousGuesses")
    private List<PreviousGuess> previousGuesses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviousGuess {
        @JsonProperty("word")
        private String word;

        @JsonProperty("feedback")
        private String feedback;

        @JsonProperty("guessNumber")
        private Integer guessNumber;
    }
}