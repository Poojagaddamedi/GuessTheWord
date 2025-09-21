package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsResponse {
    private int totalGames;
    private int gamesWon;
    private int gamesLost;
    private double winRate;
    private int currentStreak;
    private int longestStreak;
    private double averageGuesses;
}