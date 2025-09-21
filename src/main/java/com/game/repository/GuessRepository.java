package com.game.repository;

import com.game.model.Game;
import com.game.model.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuessRepository extends JpaRepository<Guess, Long> {

    // Find all guesses for a specific game, ordered by guess number
    List<Guess> findByGameOrderByGuessNumber(Game game);

    // Count guesses for a specific game
    @Query("SELECT COUNT(g) FROM Guess g WHERE g.game = :game")
    int countGuessesByGame(@Param("game") Game game);
}