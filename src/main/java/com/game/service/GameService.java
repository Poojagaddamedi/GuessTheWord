package com.game.service;

import com.game.dto.*;
import com.game.model.Game;
import com.game.model.Guess;
import com.game.model.User;
import com.game.model.Word;
import com.game.repository.GameRepository;
import com.game.repository.GuessRepository;
import com.game.repository.UserRepository;
import com.game.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final GuessRepository guessRepository;

    private static final int DAILY_GAME_LIMIT = 3;
    private static final int INITIAL_GUESSES = 5;

    /**
     * Start a new game for a user
     * 
     * @param username the username of the player
     * @return GameStartResponse with game details or error message
     * @throws IllegalStateException if daily limit is reached
     */
    public GameStartResponse startNewGame(String username) {
        // Get the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if user has reached daily limit
        LocalDate today = LocalDate.now();
        int gamesPlayedToday = gameRepository.countGamesPlayedToday(user, today);

        if (gamesPlayedToday >= DAILY_GAME_LIMIT) {
            throw new IllegalStateException(
                    "You have reached the daily limit of 3 games today. Please try again tomorrow.");
        }

        // Create a game with temporary placeholder word (will be replaced on first
        // guess)
        // This is needed because the database schema has a NOT NULL constraint on
        // word_id
        Word placeholderWord = wordRepository.findRandomWord();
        if (placeholderWord == null) {
            throw new IllegalStateException("No words available in the database.");
        }

        // Create a new game record with placeholder word
        Game newGame = new Game();
        newGame.setUser(user);
        newGame.setWord(placeholderWord); // Use placeholder word
        newGame.setDatePlayed(today);
        newGame.setRemainingGuesses(INITIAL_GUESSES);
        newGame.setIsWon(null); // Neither won nor lost initially

        // Save the game
        Game savedGame = gameRepository.save(newGame);

        // Return success response
        return new GameStartResponse(
                "New Game Started! You have 5 chances.",
                savedGame.getId(),
                savedGame.getRemainingGuesses());
    }

    /**
     * Get player's daily game status
     * 
     * @param username the username of the player
     * @return DailyStatusResponse with daily game information
     */
    public DailyStatusResponse getPlayerDailyStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        LocalDate today = LocalDate.now();
        int gamesPlayedToday = gameRepository.countGamesPlayedToday(user, today);
        int remainingGames = Math.max(0, DAILY_GAME_LIMIT - gamesPlayedToday);
        boolean canStartNewGame = gamesPlayedToday < DAILY_GAME_LIMIT;

        String message;
        if (canStartNewGame) {
            if (gamesPlayedToday == 0) {
                message = "Ready to start your first game today!";
            } else {
                message = String.format("You have %d game%s remaining today.",
                        remainingGames, remainingGames == 1 ? "" : "s");
            }
        } else {
            message = "Daily limit reached. Try again tomorrow!";
        }

        return new DailyStatusResponse(
                username,
                gamesPlayedToday,
                DAILY_GAME_LIMIT,
                remainingGames,
                canStartNewGame,
                message);
    }

    /**
     * Submit a guess for a game
     * 
     * @param username the username of the player
     * @param request  the guess request containing gameId and guessed word
     * @return GuessResponse with feedback and game status
     */
    public GuessResponse submitGuess(String username, GuessRequest request) {
        // Validate input
        if (request.getGuessedWord() == null || request.getGuessedWord().length() != 5) {
            throw new IllegalArgumentException("Guessed word must be exactly 5 letters");
        }

        String guessedWord = request.getGuessedWord().toUpperCase();

        // Get the game
        Optional<Game> gameOpt = gameRepository.findById(request.getGameId());
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Game not found");
        }

        Game game = gameOpt.get();

        // Verify user owns this game
        if (!game.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only play your own games");
        }

        // Check if game is already completed
        if (game.isCompleted()) {
            throw new IllegalStateException("This game is already completed");
        }

        // Check if user has remaining guesses
        if (game.getRemainingGuesses() <= 0) {
            throw new IllegalStateException("No remaining guesses for this game");
        }

        // Get current guess number
        int currentGuessCount = guessRepository.countGuessesByGame(game);
        int guessNumber = currentGuessCount + 1;

        // If this is the first guess, replace the placeholder word with a new random
        // target word
        String targetWord;
        if (guessNumber == 1) {
            // This is the first guess, so replace the placeholder word with a new random
            // word
            Word currentWord = game.getWord(); // This is the placeholder word
            Word newRandomWord = wordRepository.findRandomWord();

            if (newRandomWord == null) {
                throw new IllegalStateException("No words available in the database.");
            }

            // Try to get a different word than the placeholder if possible
            int attempts = 0;
            while (newRandomWord.getId().equals(currentWord.getId()) && attempts < 5) {
                newRandomWord = wordRepository.findRandomWord();
                attempts++;
            }

            // Set the new word
            game.setWord(newRandomWord);
            gameRepository.save(game);
            targetWord = newRandomWord.getWord();
        } else {
            targetWord = game.getWord().getWord();
        }

        // Generate feedback for the guess
        String feedback = generateFeedback(guessedWord, targetWord);

        // Create and save the guess
        Guess guess = new Guess();
        guess.setGame(game);
        guess.setGuessedWord(guessedWord);
        guess.setGuessNumber(guessNumber);
        guess.setFeedback(feedback);
        guessRepository.save(guess);

        // Update remaining guesses
        game.setRemainingGuesses(game.getRemainingGuesses() - 1);

        // Check if word is correct
        boolean isCorrect = guessedWord.equals(targetWord);
        boolean gameCompleted = false;
        Boolean won = null;
        String message = "";

        if (isCorrect) {
            // Player won
            game.setIsWon(true);
            gameCompleted = true;
            won = true;
            message = "🎉 Congratulations! You guessed the word correctly! The word was: " + targetWord;
        } else if (game.getRemainingGuesses() <= 0) {
            // Player lost (no more guesses)
            game.setIsWon(false);
            gameCompleted = true;
            won = false;
            message = "😞 Better luck next time! The word was: " + targetWord;
        } else {
            // Game continues
            message = "Try again! " + game.getRemainingGuesses() + " guesses remaining.";
        }

        // Save updated game
        gameRepository.save(game);

        // Get all previous guesses for response
        List<Guess> allGuesses = guessRepository.findByGameOrderByGuessNumber(game);
        List<GuessResponse.PreviousGuess> previousGuesses = allGuesses.stream()
                .map(g -> new GuessResponse.PreviousGuess(g.getGuessedWord(), g.getFeedback(), g.getGuessNumber()))
                .collect(Collectors.toList());

        // Create response using setters to avoid constructor issues
        GuessResponse response = new GuessResponse();
        response.setCorrect(isCorrect); // Fixed method name
        response.setGameCompleted(gameCompleted);
        response.setWon(won);
        response.setMessage(message);
        response.setGameId(game.getId());
        response.setRemainingGuesses(game.getRemainingGuesses());
        response.setFeedback(feedback);
        response.setTargetWord(gameCompleted ? targetWord : null);
        response.setCurrentGuess(guessedWord);
        response.setPreviousGuesses(previousGuesses);

        return response;
    }

    /**
     * Generate feedback for a guessed word vs target word
     * G = Green (correct letter, correct position)
     * O = Orange (correct letter, wrong position)
     * R = Grey (letter not in word)
     */
    private String generateFeedback(String guessed, String target) {
        StringBuilder feedback = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            char guessedChar = guessed.charAt(i);
            char targetChar = target.charAt(i);

            if (guessedChar == targetChar) {
                // Correct letter in correct position
                feedback.append('G');
            } else if (target.contains(String.valueOf(guessedChar))) {
                // Letter exists in word but wrong position
                feedback.append('O');
            } else {
                // Letter not in word
                feedback.append('R');
            }
        }

        return feedback.toString();
    }

    /**
     * Get current game status
     * 
     * @param username the username of the player
     * @param gameId   the game ID
     * @return GameStatusResponse with current game state
     */
    public GameStatusResponse getGameStatus(String username, Long gameId) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Game not found");
        }

        Game game = gameOpt.get();

        // Verify user owns this game
        if (!game.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only view your own games");
        }

        // Get all guesses for this game
        List<Guess> allGuesses = guessRepository.findByGameOrderByGuessNumber(game);
        List<GuessResponse.PreviousGuess> previousGuesses = allGuesses.stream()
                .map(g -> new GuessResponse.PreviousGuess(g.getGuessedWord(), g.getFeedback(), g.getGuessNumber()))
                .collect(Collectors.toList());

        String message = "";
        String targetWord = null;

        // Check if any guesses have been made yet (first guess is when the real word is
        // assigned)
        if (allGuesses.isEmpty()) {
            // No guesses made yet, don't show the placeholder word
            targetWord = "[Hidden until first guess]";
            message = "Game ready. " + game.getRemainingGuesses() + " guesses available.";
        }
        // Only show word if game has at least one guess or is completed with a result
        else if (game.isCompleted() && game.getIsWon() != null) {
            // Only show the word if the game is completed with a win/lose result
            // or if the user has made at least one guess
            targetWord = game.getWord().getWord();

            if (game.getIsWon()) {
                message = "Congratulations! You won this game!";
            } else {
                message = "Game over. Better luck next time!";
            }
        } else {
            message = "Game in progress. " + game.getRemainingGuesses() + " guesses remaining.";
        }

        return new GameStatusResponse(
                game.getId(),
                targetWord,
                game.getRemainingGuesses(),
                game.isCompleted(),
                game.getIsWon(),
                message,
                previousGuesses);
    }

    /**
     * Add a new word to the database (Admin functionality)
     * 
     * @param wordText the word to add (must be 5 letters)
     * @return success message
     */
    public String addWord(String wordText) {
        if (wordText == null || wordText.trim().isEmpty()) {
            throw new IllegalArgumentException("Word cannot be empty");
        }

        String normalizedWord = wordText.trim().toUpperCase();

        if (normalizedWord.length() != 5) {
            throw new IllegalArgumentException("Word must be exactly 5 letters long");
        }

        if (!normalizedWord.matches("[A-Z]+")) {
            throw new IllegalArgumentException("Word must contain only letters");
        }

        // Check if word already exists
        if (wordRepository.existsByWord(normalizedWord)) {
            throw new IllegalArgumentException("Word '" + normalizedWord + "' already exists in the database");
        }

        // Create and save new word
        Word newWord = new Word();
        newWord.setWord(normalizedWord);
        wordRepository.save(newWord);

        return "Word '" + normalizedWord + "' added successfully";
    }

    /**
     * Get player statistics - only include games with guesses made
     * 
     * @param username the player's username
     * @return player statistics
     */
    public PlayerStatsResponse getPlayerStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Get completed games
        List<Game> completedGames = gameRepository.findCompletedGamesByUser(user);

        // Filter to only include games with at least one guess made
        List<Game> gamesWithGuesses = completedGames.stream()
                .filter(game -> guessRepository.countGuessesByGame(game) > 0)
                .collect(Collectors.toList());

        int totalGames = gamesWithGuesses.size();
        int gamesWon = (int) gamesWithGuesses.stream().filter(Game::getIsWon).count();
        int gamesLost = totalGames - gamesWon;
        double winRate = totalGames > 0 ? (double) gamesWon / totalGames * 100 : 0.0;

        // Calculate current streak - using only games with guesses
        int currentStreak = calculateCurrentStreak(gamesWithGuesses);

        // Calculate longest streak - using only games with guesses
        int longestStreak = calculateLongestStreak(gamesWithGuesses);

        // Calculate average guesses for won games - use actual guess count from
        // repository
        double averageGuesses = gamesWithGuesses.stream()
                .filter(Game::getIsWon)
                .mapToDouble(game -> guessRepository.countGuessesByGame(game))
                .average()
                .orElse(0.0);

        return new PlayerStatsResponse(
                totalGames,
                gamesWon,
                gamesLost,
                Math.round(winRate * 100.0) / 100.0, // Round to 2 decimal places
                currentStreak,
                longestStreak,
                Math.round(averageGuesses * 100.0) / 100.0 // Round to 2 decimal places
        );
    }

    private int calculateCurrentStreak(List<Game> games) {
        if (games.isEmpty())
            return 0;

        // Sort games by date descending (most recent first)
        games.sort((g1, g2) -> g2.getDatePlayed().compareTo(g1.getDatePlayed()));

        int streak = 0;
        for (Game game : games) {
            if (game.getIsWon()) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private int calculateLongestStreak(List<Game> games) {
        if (games.isEmpty())
            return 0;

        // Sort games by date ascending (oldest first)
        games.sort((g1, g2) -> g1.getDatePlayed().compareTo(g2.getDatePlayed()));

        int maxStreak = 0;
        int currentStreak = 0;

        for (Game game : games) {
            if (game.getIsWon()) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }
        return maxStreak;
    }

    /**
     * Get comprehensive game history for a player - only includes games with
     * guesses
     * 
     * @param username the player's username
     * @return complete game history with all guesses and details
     */
    public GameHistoryResponse getPlayerGameHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Get all games for the user (both completed and in-progress)
        List<Game> userGames = gameRepository.findByUserOrderByDatePlayedDesc(user);

        // Filter to only include games with at least one guess made
        List<Game> gamesWithGuesses = userGames.stream()
                .filter(game -> guessRepository.countGuessesByGame(game) > 0)
                .collect(Collectors.toList());

        List<GameHistoryResponse.GameDetails> gameDetailsList = gamesWithGuesses.stream()
                .map(this::convertGameToDetails)
                .collect(Collectors.toList());

        // Calculate summary statistics - only count games with guesses
        int totalGames = gamesWithGuesses.size();
        int completedGames = (int) gamesWithGuesses.stream().filter(Game::isCompleted).count();
        int wonGames = (int) gamesWithGuesses.stream().filter(game -> Boolean.TRUE.equals(game.getIsWon())).count();
        int lostGames = (int) gamesWithGuesses.stream().filter(game -> Boolean.FALSE.equals(game.getIsWon())).count();

        return new GameHistoryResponse(
                username,
                totalGames,
                completedGames,
                wonGames,
                lostGames,
                gameDetailsList);
    }

    /**
     * Convert a Game entity to GameDetails DTO
     */
    private GameHistoryResponse.GameDetails convertGameToDetails(Game game) {
        List<Guess> guesses = guessRepository.findByGameOrderByGuessNumber(game);

        List<GameHistoryResponse.GuessDetails> guessDetailsList = guesses.stream()
                .map(guess -> new GameHistoryResponse.GuessDetails(
                        guess.getGuessNumber(),
                        guess.getGuessedWord(),
                        guess.getFeedback() != null ? guess.getFeedback() : "",
                        guess.getFeedback() != null ? convertFeedbackToDisplay(guess.getFeedback()) : ""))
                .collect(Collectors.toList());

        // Use the remaining_guesses field from the game
        int guessesUsed = INITIAL_GUESSES
                - (game.getRemainingGuesses() != null ? game.getRemainingGuesses() : INITIAL_GUESSES);

        // Handle word display logic
        String wordToShow;
        // Only show word if game has at least one guess or is completed
        // This prevents showing the target word for games that were started but never
        // played
        if (guesses.isEmpty() && !game.isCompleted()) {
            wordToShow = "[Hidden until first guess]"; // Hide word if no guesses were made and game is not completed
        } else {
            wordToShow = game.getWord().getWord(); // Show word normally
        }

        return new GameHistoryResponse.GameDetails(
                game.getId(),
                game.getDatePlayed(),
                wordToShow,
                game.isCompleted(),
                game.getIsWon(),
                game.getRemainingGuesses() != null ? game.getRemainingGuesses() : 0,
                guessesUsed,
                guessDetailsList);
    }

    /**
     * Convert feedback string (GROGO) to display format for frontend
     */
    private String convertFeedbackToDisplay(String feedback) {
        return feedback.replace('G', 'G') // Green - correct position
                .replace('O', 'Y') // Yellow - correct letter, wrong position
                .replace('R', 'R'); // Red - letter not in word
    }

    /**
     * Get comprehensive admin reports with all system data
     * 
     * @return comprehensive admin reports
     */
    public AdminReportsResponse getComprehensiveAdminReports() {
        // Get all users
        List<User> allUsers = userRepository.findAll();

        // Separate users who have played games from just registered users
        // STRICT FILTERING: Only include users who have made at least 1 guess in any
        // game
        List<AdminReportsResponse.PlayerReport> playerReports = allUsers.stream()
                .filter(user -> {
                    // Only include users who have made at least 1 guess in any game
                    List<Game> userGames = gameRepository.findByUser(user);
                    if (userGames.isEmpty()) {
                        return false;
                    }

                    // Check if any game has at least one guess
                    return userGames.stream().anyMatch(game -> guessRepository.countGuessesByGame(game) > 0);
                })
                .map(this::convertUserToPlayerReport)
                .collect(Collectors.toList());

        // Get all games - ONLY those with at least one guess made
        List<Game> allGames = gameRepository.findAll();
        List<AdminReportsResponse.GameReport> gameReports = allGames.stream()
                .filter(game -> game.getUser() != null && // Ensure game has a user
                        guessRepository.countGuessesByGame(game) > 0 // Only include games with at least one guess
                )
                .map(this::convertGameToGameReport)
                .collect(Collectors.toList());

        // Calculate system statistics
        AdminReportsResponse.SystemStatistics systemStats = calculateSystemStatistics(allUsers, allGames);

        // Get recent user registrations (last 30 days) - ALL users regardless of game
        // activity
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<AdminReportsResponse.UserRegistration> recentRegistrations = allUsers.stream()
                .filter(user -> user.getCreatedAt() != null
                        && !user.getCreatedAt().toLocalDate().isBefore(thirtyDaysAgo))
                .map(user -> {
                    // Add game count to registration info
                    List<Game> userGames = gameRepository.findByUser(user);
                    return new AdminReportsResponse.UserRegistration(
                            user.getUsername(),
                            user.getRole().name(),
                            user.getCreatedAt(),
                            userGames.size()); // Add game count
                })
                .collect(Collectors.toList());

        return new AdminReportsResponse(
                playerReports,
                gameReports,
                recentRegistrations,
                systemStats);
    }

    /**
     * Convert User to PlayerReport
     */
    private AdminReportsResponse.PlayerReport convertUserToPlayerReport(User user) {
        List<Game> userGames = gameRepository.findByUser(user);

        // Filter games to only include those with at least one guess made
        List<Game> gamesWithGuesses = userGames.stream()
                .filter(game -> guessRepository.countGuessesByGame(game) > 0)
                .collect(Collectors.toList());

        int totalGames = gamesWithGuesses.size();
        int wonGames = (int) gamesWithGuesses.stream().filter(game -> Boolean.TRUE.equals(game.getIsWon())).count();

        // Last played date from games with guesses
        LocalDate lastPlayed = gamesWithGuesses.stream()
                .map(Game::getDatePlayed)
                .max(LocalDate::compareTo)
                .orElse(null);

        return new AdminReportsResponse.PlayerReport(
                user.getUsername(),
                user.getRole().name(),
                user.getCreatedAt(),
                totalGames,
                wonGames,
                lastPlayed);
    }

    /**
     * Convert Game to GameReport - only if it has guesses
     */
    private AdminReportsResponse.GameReport convertGameToGameReport(Game game) {
        // Get actual guesses count from GuessRepository
        int actualGuessesCount = guessRepository.countGuessesByGame(game);

        // Handle case where no guesses made yet (placeholder word should be hidden)
        String wordToShow;
        if (actualGuessesCount == 0) {
            wordToShow = "[Hidden until first guess]";
        } else {
            wordToShow = game.getWord().getWord();
        }

        // Use actual guesses made rather than calculating from remainingGuesses
        return new AdminReportsResponse.GameReport(
                game.getId(),
                game.getUser().getUsername(),
                wordToShow,
                game.getDatePlayed(),
                game.isCompleted(),
                game.getIsWon(),
                actualGuessesCount);
    }

    /**
     * Calculate comprehensive system statistics - only count games with guesses
     */
    private AdminReportsResponse.SystemStatistics calculateSystemStatistics(List<User> allUsers, List<Game> allGames) {
        int totalUsers = allUsers.size();
        int totalPlayers = (int) allUsers.stream().filter(user -> user.getRole() == User.Role.PLAYER).count();
        int totalAdmins = (int) allUsers.stream().filter(user -> user.getRole() == User.Role.ADMIN).count();

        // Filter to only include games with at least one guess
        List<Game> gamesWithGuesses = allGames.stream()
                .filter(game -> guessRepository.countGuessesByGame(game) > 0)
                .collect(Collectors.toList());

        int totalGames = gamesWithGuesses.size();
        int completedGames = (int) gamesWithGuesses.stream().filter(Game::isCompleted).count();
        int wonGames = (int) gamesWithGuesses.stream().filter(game -> Boolean.TRUE.equals(game.getIsWon())).count();
        int lostGames = (int) gamesWithGuesses.stream().filter(game -> Boolean.FALSE.equals(game.getIsWon())).count();

        double overallWinRate = completedGames > 0 ? (double) wonGames / completedGames * 100 : 0.0;

        // Games played today (with guesses)
        LocalDate today = LocalDate.now();
        int gamesToday = (int) gamesWithGuesses.stream().filter(game -> game.getDatePlayed().equals(today)).count();

        // Count total words in the database
        long totalWords = wordRepository.count();

        return new AdminReportsResponse.SystemStatistics(
                totalUsers,
                totalPlayers,
                totalAdmins,
                totalGames,
                completedGames,
                wonGames,
                lostGames,
                Math.round(overallWinRate * 100.0) / 100.0,
                gamesToday,
                totalWords);
    }

    /**
     * Get players filtered by daily game count for admin
     * 
     * @param gameCount filter by daily game count (1, 2, or 3)
     * @return filtered list of players based on daily game count
     */
    public List<AdminReportsResponse.PlayerReport> getPlayersByDailyGameCount(int gameCount) {
        if (gameCount < 1 || gameCount > 3) {
            throw new IllegalArgumentException("Game count must be between 1 and 3");
        }

        LocalDate today = LocalDate.now();
        List<User> allPlayers = userRepository.findByRole(User.Role.PLAYER);

        return allPlayers.stream()
                .filter(user -> {
                    // Only include users who have made at least 1 guess in any game
                    List<Game> userGames = gameRepository.findByUser(user);
                    if (userGames.isEmpty()) {
                        return false;
                    }

                    // Check if any game has at least one guess
                    boolean hasGuesses = userGames.stream()
                            .anyMatch(game -> guessRepository.countGuessesByGame(game) > 0);
                    if (!hasGuesses) {
                        return false;
                    }

                    // Filter by today's games with guesses count
                    List<Game> todayGames = userGames.stream()
                            .filter(game -> game.getDatePlayed().equals(today))
                            .collect(Collectors.toList());

                    // Count games that have at least one guess
                    int gamesWithGuesses = 0;
                    for (Game game : todayGames) {
                        if (guessRepository.countGuessesByGame(game) > 0) {
                            gamesWithGuesses++;
                        }
                    }

                    return gamesWithGuesses == gameCount;
                })
                .map(this::convertUserToPlayerReport)
                .collect(Collectors.toList());
    }

    /**
     * Get all player activities for admin view - ONLY users who have made at least
     * 1 guess
     * 
     * @return all player activities and game history
     */
    public List<AdminReportsResponse.PlayerReport> getAllPlayerActivities() {
        List<User> allPlayers = userRepository.findByRole(User.Role.PLAYER);
        return allPlayers.stream()
                .filter(user -> {
                    // ONLY include users who have made at least 1 guess in any game
                    List<Game> userGames = gameRepository.findByUser(user);
                    if (userGames.isEmpty()) {
                        return false;
                    }

                    // Check if any game has at least one guess
                    return userGames.stream().anyMatch(game -> guessRepository.countGuessesByGame(game) > 0);
                })
                .map(this::convertUserToPlayerReport)
                .collect(Collectors.toList());
    }

    /**
     * Get system statistics for admin dashboard
     * 
     * @return system-wide statistics
     */
    public AdminReportsResponse.SystemStatistics getSystemStatistics() {
        List<User> allUsers = userRepository.findAll();
        List<Game> allGames = gameRepository.findAll();
        return calculateSystemStatistics(allUsers, allGames);
    }

    /**
     * Get players who won games with specific number of guesses
     * 
     * @param guessCount the number of guesses used to win (1, 2, or 3)
     * @return list of players who won with the specified number of guesses
     */
    public List<AdminReportsResponse.WinnerReport> getPlayersWhoWonWithGuessCount(int guessCount) {
        if (guessCount < 1 || guessCount > 5) {
            throw new IllegalArgumentException("Guess count must be between 1 and 5");
        }

        List<User> winners = gameRepository.findUsersWhoWonWithGuessCount(guessCount);

        return winners.stream()
                .map(user -> {
                    int totalGames = gameRepository.findByUser(user).size();
                    int totalCompleted = gameRepository.findCompletedGamesByUser(user).size();
                    int winsWithGuessCount = gameRepository.countGamesWonWithGuessCount(user, guessCount);

                    return new AdminReportsResponse.WinnerReport(
                            user.getUsername(),
                            totalGames,
                            totalCompleted,
                            guessCount,
                            winsWithGuessCount);
                })
                .collect(Collectors.toList());
    }
}