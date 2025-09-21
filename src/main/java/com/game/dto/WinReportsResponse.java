package com.game.dto;

import lombok.Data;
import java.util.List;

@Data
public class WinReportsResponse {
    private int totalUsers;
    private int totalWinners;
    private int totalDistinctWinners; // For compatibility with test case
    private int totalWins; // For compatibility with test case

    // Reports grouped by guess count
    private List<GuessCountWinners> winsByGuessCount;

    // Users who won with exactly one guess
    private List<WinnerDetail> oneGuessWinners;

    // Users who won with exactly two guesses
    private List<WinnerDetail> twoGuessWinners;

    // Users who won with exactly three guesses
    private List<WinnerDetail> threeGuessWinners;

    // Default constructor
    public WinReportsResponse() {
    }

    // Constructor for test case
    public WinReportsResponse(int totalUsers, int totalWinners, List<GuessCountWinners> winsByGuessCount,
            List<WinnerDetail> oneGuessWinners, List<WinnerDetail> twoGuessWinners,
            List<WinnerDetail> threeGuessWinners) {
        this.totalUsers = totalUsers;
        this.totalWinners = totalWinners;
        this.totalDistinctWinners = totalWinners; // Set same as totalWinners for backward compatibility
        this.winsByGuessCount = winsByGuessCount;
        this.oneGuessWinners = oneGuessWinners;
        this.twoGuessWinners = twoGuessWinners;
        this.threeGuessWinners = threeGuessWinners;
    }

    @Data
    public static class GuessCountWinners {
        private int guessCount;
        private int winnersCount;
        private int totalWins;
        private int uniqueWinners;
        private List<WinnerDetail> winners;

        public GuessCountWinners() {
        }

        // Constructor that matches the test case
        public GuessCountWinners(int guessCount, int winnersCount) {
            this.guessCount = guessCount;
            this.winnersCount = winnersCount;
        }

        // Full constructor
        public GuessCountWinners(int guessCount, int winnersCount, int totalWins,
                int uniqueWinners, List<WinnerDetail> winners) {
            this.guessCount = guessCount;
            this.winnersCount = winnersCount;
            this.totalWins = totalWins;
            this.uniqueWinners = uniqueWinners;
            this.winners = winners;
        }
    }

    @Data
    public static class WinnerDetail {
        private String username;
        private int totalGames;
        private int totalWins;
        private int winsWithSpecificGuessCount;
        private double winRate;

        public WinnerDetail() {
        }

        // Constructor for the test case
        public WinnerDetail(String username, int totalGames, int totalWins,
                int winsWithSpecificGuessCount, double winRate) {
            this.username = username;
            this.totalGames = totalGames;
            this.totalWins = totalWins;
            this.winsWithSpecificGuessCount = winsWithSpecificGuessCount;
            this.winRate = winRate;
        }
    }
}