package com.game.repository;

import com.game.model.Game;
import com.game.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    // Find games played by a specific user on a specific date
    List<Game> findByUserAndDatePlayed(User user, LocalDate date);

    // Find completed games by user (where isWon is not null)
    @Query("SELECT g FROM Game g WHERE g.user = :user AND g.isWon IS NOT NULL")
    List<Game> findCompletedGamesByUser(@Param("user") User user);

    // Count games played by a user on a specific date
    @Query("SELECT COUNT(g) FROM Game g WHERE g.user = :user AND g.datePlayed = :date")
    int countGamesPlayedToday(@Param("user") User user, @Param("date") LocalDate date);

    // Find users who won games within specified number of guesses
    @Query("SELECT g.user FROM Game g WHERE g.isWon = true AND (5 - g.remainingGuesses) = :guessCount GROUP BY g.user")
    List<User> findUsersWhoWonWithGuessCount(@Param("guessCount") int guessCount);

    // Count games won by a user with specific guess count
    @Query("SELECT COUNT(g) FROM Game g WHERE g.user = :user AND g.isWon = true AND (5 - g.remainingGuesses) = :guessCount")
    int countGamesWonWithGuessCount(@Param("user") User user, @Param("guessCount") int guessCount);

    // Count total wins for a user
    @Query("SELECT COUNT(g) FROM Game g WHERE g.user = :user AND g.isWon = true")
    int countTotalWins(@Param("user") User user);

    // Count distinct users who have won games
    @Query("SELECT COUNT(DISTINCT g.user) FROM Game g WHERE g.isWon = true")
    int countDistinctWinners();

    // Count wins by guess count across all users
    @Query("SELECT (5 - g.remainingGuesses) as guessCount, COUNT(g) as winCount " +
            "FROM Game g WHERE g.isWon = true GROUP BY (5 - g.remainingGuesses) ORDER BY guessCount")
    List<Object[]> countWinsByGuessCount();

    // Find all games by user ordered by date played (descending)
    List<Game> findByUserOrderByDatePlayedDesc(User user);

    // Find all games by user
    List<Game> findByUser(User user);
}