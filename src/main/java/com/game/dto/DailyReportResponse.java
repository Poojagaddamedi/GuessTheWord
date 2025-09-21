package com.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportResponse {
    private LocalDate date;
    private int totalUsers; // Users who played on this date
    private int totalGames; // Total games played on this date
    private int gamesWon; // Games won on this date
    private int gamesLost; // Games lost on this date
    private int gamesInProgress; // Games still in progress
    private double winRate; // Win rate percentage for the day
    private int totalGuesses; // Total guesses made on this date
    private double averageGuessesPerGame; // Average guesses per completed game
}