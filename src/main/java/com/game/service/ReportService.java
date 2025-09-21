package com.game.service;

import com.game.dto.DailyReportResponse;
import com.game.dto.UserReportResponse;
import com.game.dto.WinReportsResponse;
import com.game.model.Game;
import com.game.model.Guess;
import com.game.model.User;
import com.game.repository.GameRepository;
import com.game.repository.GuessRepository;
import com.game.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final GameRepository gameRepository;
        private final GuessRepository guessRepository;
        private final UserRepository userRepository;

        /**
         * Generate daily report for a specific date
         * 
         * @param date the date to generate report for
         * @return DailyReportResponse with statistics for the day
         */
        public DailyReportResponse getDailyReport(LocalDate date) {
                // Get all games played on the specified date
                List<Game> gamesOnDate = gameRepository.findAll().stream()
                                .filter(game -> game.getDatePlayed().equals(date))
                                .collect(Collectors.toList());

                // Count unique users who played on this date
                int totalUsers = (int) gamesOnDate.stream()
                                .map(Game::getUser)
                                .distinct()
                                .count();

                int totalGames = gamesOnDate.size();

                // Count games by status
                int gamesWon = (int) gamesOnDate.stream()
                                .filter(game -> Boolean.TRUE.equals(game.getIsWon()))
                                .count();

                int gamesLost = (int) gamesOnDate.stream()
                                .filter(game -> Boolean.FALSE.equals(game.getIsWon()))
                                .count();

                int gamesInProgress = totalGames - gamesWon - gamesLost;

                // Calculate win rate
                double winRate = totalGames > 0 ? (gamesWon * 100.0 / totalGames) : 0.0;

                // Count total guesses made on this date
                int totalGuessesCount = gamesOnDate.stream()
                                .mapToInt(game -> guessRepository.countGuessesByGame(game))
                                .sum();

                // Calculate average guesses per completed game
                int completedGames = gamesWon + gamesLost;
                double averageGuessesPerGame = completedGames > 0 ? (totalGuessesCount * 1.0 / completedGames) : 0.0;

                return new DailyReportResponse(date, totalUsers, totalGames, gamesWon, gamesLost,
                                gamesInProgress, winRate, totalGuessesCount, averageGuessesPerGame);
        }

        /**
         * Generate user-specific report
         * 
         * @param username the username to generate report for
         * @return UserReportResponse with user's game history and statistics
         */
        public UserReportResponse getUserReport(String username) {
                // Find the user
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

                // Get all games for this user
                List<Game> userGames = gameRepository.findAll().stream()
                                .filter(game -> game.getUser().equals(user))
                                .collect(Collectors.toList());

                // Group games by date
                Map<LocalDate, List<Game>> gamesByDate = userGames.stream()
                                .collect(Collectors.groupingBy(Game::getDatePlayed));

                // Generate report for each date
                List<UserReportResponse.UserGameReport> gameReports = gamesByDate.entrySet().stream()
                                .map(entry -> {
                                        LocalDate date = entry.getKey();
                                        List<Game> gamesOnDate = entry.getValue();

                                        int wordsAttempted = gamesOnDate.size();
                                        int correctGuesses = (int) gamesOnDate.stream()
                                                        .filter(game -> Boolean.TRUE.equals(game.getIsWon()))
                                                        .count();

                                        int totalGuesses = gamesOnDate.stream()
                                                        .mapToInt(game -> guessRepository.countGuessesByGame(game))
                                                        .sum();

                                        // Get game details
                                        List<UserReportResponse.GameDetails> gameDetails = gamesOnDate.stream()
                                                        .map(game -> {
                                                                String wordToShow = (game.getWord() == null)
                                                                                ? "[No word yet]"
                                                                                : game.getWord().getWord();
                                                                return new UserReportResponse.GameDetails(
                                                                                game.getId(),
                                                                                wordToShow,
                                                                                Boolean.TRUE.equals(game.getIsWon()),
                                                                                guessRepository.countGuessesByGame(
                                                                                                game),
                                                                                game.getDatePlayed());
                                                        })
                                                        .collect(Collectors.toList());

                                        return new UserReportResponse.UserGameReport(
                                                        date, wordsAttempted, correctGuesses, totalGuesses,
                                                        gameDetails);
                                })
                                .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate())) // Sort by date descending
                                .collect(Collectors.toList());

                return new UserReportResponse(username, gameReports);
        }

        /**
         * Generate comprehensive win reports for all users
         * 
         * @return detailed win reports grouped by guess count
         */
        public WinReportsResponse getComprehensiveWinReports() {
                // Get total users count
                int totalUsers = (int) userRepository.count();

                // Count distinct winners
                int totalWinners = gameRepository.countDistinctWinners();

                // Get win distribution by guess count
                List<WinReportsResponse.GuessCountWinners> winsByGuessCount = new ArrayList<>();
                List<Object[]> winCounts = gameRepository.countWinsByGuessCount();

                for (Object[] result : winCounts) {
                        int guessCount = ((Number) result[0]).intValue();
                        int count = ((Number) result[1]).intValue();
                        winsByGuessCount.add(new WinReportsResponse.GuessCountWinners(guessCount, count));
                }

                // Get users who won with specific guess counts
                List<WinReportsResponse.WinnerDetail> oneGuessWinners = getUsersWhoWonWithGuessCount(1);
                List<WinReportsResponse.WinnerDetail> twoGuessWinners = getUsersWhoWonWithGuessCount(2);
                List<WinReportsResponse.WinnerDetail> threeGuessWinners = getUsersWhoWonWithGuessCount(3);

                return new WinReportsResponse(
                                totalUsers,
                                totalWinners,
                                winsByGuessCount,
                                oneGuessWinners,
                                twoGuessWinners,
                                threeGuessWinners);
        }

        /**
         * Get list of users who won with specific guess count
         * 
         * @param guessCount the number of guesses used to win (1-5)
         * @return list of users with their win statistics
         */
        private List<WinReportsResponse.WinnerDetail> getUsersWhoWonWithGuessCount(int guessCount) {
                List<User> winners = gameRepository.findUsersWhoWonWithGuessCount(guessCount);

                return winners.stream().map(user -> {
                        // Get user's total games
                        List<Game> userGames = gameRepository.findByUser(user);
                        int totalGames = userGames.size();

                        // Get total wins
                        int totalWins = gameRepository.countTotalWins(user);

                        // Get wins with specific guess count
                        int winsWithGuessCount = gameRepository.countGamesWonWithGuessCount(user, guessCount);

                        // Calculate win rate
                        double winRate = totalGames > 0 ? (double) totalWins / totalGames * 100 : 0.0;

                        return new WinReportsResponse.WinnerDetail(
                                        user.getUsername(),
                                        totalGames,
                                        totalWins,
                                        winsWithGuessCount,
                                        Math.round(winRate * 100.0) / 100.0 // Round to 2 decimal places
                        );
                }).collect(Collectors.toList());
        }
}