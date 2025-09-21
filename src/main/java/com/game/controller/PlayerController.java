package com.game.controller;

import com.game.dto.PlayerStatsResponse;
import com.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final GameService gameService;

    @GetMapping("/game")
    public String playGame() {
        return "Welcome Player 🎮, start your game!";
    }

    /**
     * Get player statistics
     * 
     * @return player statistics including games played, won, win rate, etc.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getPlayerStats() {
        try {
            // Get authenticated username from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            PlayerStatsResponse stats = gameService.getPlayerStats(username);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get player stats: " + e.getMessage()));
        }
    }

    /**
     * Get comprehensive game history for the player
     * 
     * @return detailed game history with all guesses and results
     */
    @GetMapping("/history")
    public ResponseEntity<?> getGameHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            var history = gameService.getPlayerGameHistory(username);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get game history: " + e.getMessage()));
        }
    }

    /**
     * Get player's daily game status (games played today and remaining)
     * 
     * @return daily game status including games played today and remaining games
     */
    @GetMapping("/daily-status")
    public ResponseEntity<?> getDailyGameStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            var dailyStatus = gameService.getPlayerDailyStatus(username);
            return ResponseEntity.ok(dailyStatus);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get daily status: " + e.getMessage()));
        }
    }

    /**
     * Simple error response class
     */
    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
