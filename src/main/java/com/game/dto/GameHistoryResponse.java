package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryResponse {
    private String username;
    private int totalGames;
    private int completedGames;
    private int wonGames;
    private int lostGames;
    private List<GameDetails> games;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameDetails {
        private Long gameId;
        private LocalDate datePlayed;
        private String targetWord;
        private boolean completed;
        private Boolean won;
        private int remainingGuesses;
        private int guessesUsed;
        private List<GuessDetails> guesses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuessDetails {
        private int guessNumber;
        private String guessedWord;
        private String feedback;
        private String feedbackDisplay;
    }
}