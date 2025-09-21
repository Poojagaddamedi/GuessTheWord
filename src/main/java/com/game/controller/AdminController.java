package com.game.controller;

import com.game.dto.DailyReportResponse;
import com.game.dto.UserReportResponse;
import com.game.dto.GameHistoryResponse;
import com.game.dto.AddWordRequest;
import com.game.dto.WinReportsResponse;
import com.game.service.ReportService;
import com.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ReportService reportService;
    private final GameService gameService;

    @GetMapping("/report")
    public String getReport() {
        return "Admin-only Report Data ✅";
    }

    /**
     * Add a new word to the game database
     * 
     * @param request the word to add
     * @return success message
     */
    @PostMapping("/words")
    public ResponseEntity<?> addWord(@RequestBody AddWordRequest request) {
        try {
            String result = gameService.addWord(request.getWord());
            return ResponseEntity.ok(new AddWordResponse(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to add word: " + e.getMessage()));
        }
    }

    /**
     * Get daily report for a specific date
     * 
     * @param date the date for the report (format: yyyy-MM-dd)
     * @return daily report with user and guess statistics
     */
    @GetMapping("/reports/daily")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailyReportResponse report = reportService.getDailyReport(date);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user-specific report
     * 
     * @param username the username to generate report for
     * @return user report with game details and statistics
     */
    @GetMapping("/reports/user/{username}")
    public ResponseEntity<UserReportResponse> getUserReport(@PathVariable String username) {
        try {
            UserReportResponse report = reportService.getUserReport(username);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get detailed game history for a user (admin access to player game history)
     * This includes all guessed words for each game
     * 
     * @param username the username to get detailed history for
     * @return complete game history with all guessed words
     */
    @GetMapping("/player-history/{username}")
    public ResponseEntity<GameHistoryResponse> getPlayerGameHistory(@PathVariable String username) {
        try {
            GameHistoryResponse history = gameService.getPlayerGameHistory(username);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get comprehensive admin reports with all system data
     * 
     * @return comprehensive reports including all players, games, and statistics
     */
    @GetMapping("/comprehensive-reports")
    public ResponseEntity<?> getComprehensiveReports() {
        try {
            var reports = gameService.getComprehensiveAdminReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to generate comprehensive reports: " + e.getMessage()));
        }
    }

    /**
     * Get all player activities and detailed game history - ONLY users who have
     * played games
     * 
     * @return detailed reports of all player activities (excludes users with 0
     *         games)
     */
    @GetMapping("/player-activities")
    public ResponseEntity<?> getAllPlayerActivities() {
        try {
            var activities = gameService.getAllPlayerActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get player activities: " + e.getMessage()));
        }
    }

    /**
     * Get players filtered by daily game count
     * 
     * @param gameCount filter by daily game count (1, 2, or 3)
     * @return filtered list of players based on their daily game count
     */
    @GetMapping("/players-by-daily-count/{gameCount}")
    public ResponseEntity<?> getPlayersByDailyGameCount(@PathVariable int gameCount) {
        try {
            if (gameCount < 1 || gameCount > 3) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Game count must be between 1 and 3"));
            }

            var players = gameService.getPlayersByDailyGameCount(gameCount);
            return ResponseEntity.ok(new DailyGameFilterResponse(
                    gameCount,
                    players.size(),
                    players));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get players by daily game count: " + e.getMessage()));
        }
    }

    /**
     * Get system statistics and analytics
     * 
     * @return system-wide statistics and analytics
     */
    @GetMapping("/system-stats")
    public ResponseEntity<?> getSystemStatistics() {
        try {
            var stats = gameService.getSystemStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get system statistics: " + e.getMessage()));
        }
    }

    /**
     * Get players who won games with specific guess count (1-5 guesses)
     * This helps analyze which players are winning most efficiently
     * 
     * @param guessCount the number of guesses used to win (1-5)
     * @return list of players who won with the specified guess count
     */
    @GetMapping("/winners-by-guess-count/{guessCount}")
    public ResponseEntity<?> getPlayersWhoWonWithGuessCount(@PathVariable int guessCount) {
        try {
            if (guessCount < 1 || guessCount > 5) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Guess count must be between 1 and 5"));
            }

            var winners = gameService.getPlayersWhoWonWithGuessCount(guessCount);
            return ResponseEntity.ok(new WinnersByGuessCountResponse(guessCount, winners.size(), winners));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get winners by guess count: " + e.getMessage()));
        }
    }

    /**
     * Get comprehensive win reports showing users who won in 1, 2, or 3 guesses
     * This provides detailed statistics about the most efficient players
     * 
     * @return detailed win reports grouped by guess count
     */
    @GetMapping("/win-reports")
    public ResponseEntity<?> getWinReports() {
        try {
            var reports = reportService.getComprehensiveWinReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to generate win reports: " + e.getMessage()));
        }
    }

    /**
     * Response classes for word management and filtering
     */
    private static class AddWordResponse {
        private final String message;

        public AddWordResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Response class for daily game count filtering
     */
    private static class DailyGameFilterResponse {
        private final int filterGameCount;
        private final int totalPlayersFound;
        private final java.util.List<com.game.dto.AdminReportsResponse.PlayerReport> players;

        public DailyGameFilterResponse(int filterGameCount, int totalPlayersFound,
                java.util.List<com.game.dto.AdminReportsResponse.PlayerReport> players) {
            this.filterGameCount = filterGameCount;
            this.totalPlayersFound = totalPlayersFound;
            this.players = players;
        }

        public int getFilterGameCount() {
            return filterGameCount;
        }

        public int getTotalPlayersFound() {
            return totalPlayersFound;
        }

        public java.util.List<com.game.dto.AdminReportsResponse.PlayerReport> getPlayers() {
            return players;
        }
    }

    /**
     * Response class for winners by guess count
     */
    private static class WinnersByGuessCountResponse {
        private final int guessCount;
        private final int totalWinnersFound;
        private final java.util.List<com.game.dto.AdminReportsResponse.WinnerReport> winners;

        public WinnersByGuessCountResponse(int guessCount, int totalWinnersFound,
                java.util.List<com.game.dto.AdminReportsResponse.WinnerReport> winners) {
            this.guessCount = guessCount;
            this.totalWinnersFound = totalWinnersFound;
            this.winners = winners;
        }

        public int getGuessCount() {
            return guessCount;
        }

        public int getTotalWinnersFound() {
            return totalWinnersFound;
        }

        public java.util.List<com.game.dto.AdminReportsResponse.WinnerReport> getWinners() {
            return winners;
        }
    }

    /**
     * Response class for winner filtering
     */
    private static class WinnerFilterResponse {
        private final int guessCount;
        private final int totalWinnersFound;
        private final java.util.List<?> winners;

        public WinnerFilterResponse(int guessCount, int totalWinnersFound, java.util.List<?> winners) {
            this.guessCount = guessCount;
            this.totalWinnersFound = totalWinnersFound;
            this.winners = winners;
        }

        public int getGuessCount() {
            return guessCount;
        }

        public int getTotalWinnersFound() {
            return totalWinnersFound;
        }

        public java.util.List<?> getWinners() {
            return winners;
        }
    }
}
