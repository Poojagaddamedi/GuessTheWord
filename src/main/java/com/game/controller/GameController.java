package com.game.controller;

import com.game.dto.*;
import com.game.security.JwtUtil;
import com.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final JwtUtil jwtUtil;

    /**
     * Endpoint for starting a new game
     * Requires JWT authentication
     * 
     * @return ResponseEntity with game details or error message
     */
    @PostMapping("/start")
    public ResponseEntity<?> startGame() {
        try {
            // Get authenticated username from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Start a new game
            GameStartResponse response = gameService.startNewGame(username);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // Daily limit reached or other game rule violation
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to start game: " + e.getMessage()));
        }
    }

    /**
     * Submit a guess for a game
     * Requires JWT authentication
     * 
     * @param request the guess request containing gameId and guessed word
     * @return ResponseEntity with guess feedback and game status
     */
    @PostMapping("/guess")
    public ResponseEntity<?> submitGuess(@RequestBody GuessRequest request) {
        try {
            // Get authenticated username from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Submit the guess
            GuessResponse response = gameService.submitGuess(username, request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Invalid request or game rule violation
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to submit guess: " + e.getMessage()));
        }
    }

    /**
     * Get current game status
     * Requires JWT authentication
     * 
     * @param gameId the game ID
     * @return ResponseEntity with current game state
     */
    @GetMapping("/status/{gameId}")
    public ResponseEntity<?> getGameStatus(@PathVariable Long gameId) {
        try {
            // Get authenticated username from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Get game status
            GameStatusResponse response = gameService.getGameStatus(username, gameId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Game not found or access denied
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Failed to get game status: " + e.getMessage()));
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