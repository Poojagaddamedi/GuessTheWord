package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReportResponse {
    private String username;
    private List<UserGameReport> gameReports;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGameReport {
        private LocalDate date;
        private int wordsAttempted;
        private int correctGuesses;
        private int totalGuesses;
        private List<GameDetails> games;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameDetails {
        private Long gameId;
        private String targetWord;
        private boolean won;
        private int guessesUsed;
        private LocalDate datePlayed;
    }
}