package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatusResponse {
    private String username;
    private int gamesPlayedToday;
    private int dailyLimit;
    private int remainingGames;
    private boolean canStartNewGame;
    private String message;
}