package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportsResponse {
    private List<PlayerReport> playerReports;
    private List<GameReport> gameReports;
    private List<UserRegistration> recentRegistrations;
    private SystemStatistics systemStatistics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerReport {
        private String username;
        private String role;
        private LocalDateTime createdAt;
        private int totalGames;
        private int wonGames;
        private LocalDate lastPlayed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameReport {
        private Long gameId;
        private String username;
        private String targetWord;
        private LocalDate datePlayed;
        private boolean completed;
        private Boolean won;
        private int guessesUsed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegistration {
        private String username;
        private String role;
        private LocalDateTime createdAt;
        private int gamesPlayed; // Number of games played by this user
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemStatistics {
        private int totalUsers;
        private int totalPlayers;
        private int totalAdmins;
        private int totalGames;
        private int completedGames;
        private int wonGames;
        private int lostGames;
        private double overallWinRate;
        private int gamesToday;
        private long totalWords;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinnerReport {
        private String username;
        private int totalGames;
        private int completedGames;
        private int guessCount; // How many guesses were used to win
        private int winsWithThisGuessCount; // Number of wins with this guess count
    }
}