package com.game.config;

import com.game.model.Word;
import com.game.repository.WordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    /**
     * Initialize the database with 20 five-letter English words in uppercase
     */
    @Bean
    public CommandLineRunner initWords(WordRepository wordRepository) {
        return args -> {
            // Check if words already exist
            if (wordRepository.count() == 0) {
                // List of 20 five-letter words in uppercase
                List<String> wordList = Arrays.asList(
                        "APPLE", "BEACH", "CLOCK", "DRINK", "EARTH",
                        "FRUIT", "GLOBE", "HEART", "INBOX", "JUDGE",
                        "KNIFE", "LEMON", "MUSIC", "NIGHT", "OCEAN",
                        "PIANO", "QUICK", "RIVER", "STEEL", "TABLE");

                // Save each word to database
                wordList.forEach(wordText -> {
                    Word word = new Word();
                    word.setWord(wordText);
                    wordRepository.save(word);
                });

                System.out.println("Initialized database with " + wordList.size() + " words");
            }
        };
    }
}