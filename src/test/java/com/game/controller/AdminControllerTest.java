package com.game.controller;

import com.game.dto.WinReportsResponse;
import com.game.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private com.game.service.GameService gameService;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testGetWinReports() throws Exception {
        // Create sample win reports data
        WinReportsResponse winReports = new WinReportsResponse();

        // Setup guess count 1 winners (for winsByGuessCount)
        WinReportsResponse.GuessCountWinners guessCount1Winners = new WinReportsResponse.GuessCountWinners(1, 2);

        // Setup player details for 1-guess winners
        WinReportsResponse.WinnerDetail winner1 = new WinReportsResponse.WinnerDetail("player1", 4, 3, 3, 75.0);
        WinReportsResponse.WinnerDetail winner2 = new WinReportsResponse.WinnerDetail("player2", 4, 2, 2, 50.0);

        List<WinReportsResponse.WinnerDetail> oneGuessWinnersList = Arrays.asList(winner1, winner2);

        // Setup guess count 2 winners (for winsByGuessCount)
        WinReportsResponse.GuessCountWinners guessCount2Winners = new WinReportsResponse.GuessCountWinners(2, 3);

        // Setup player details for 2-guess winners
        WinReportsResponse.WinnerDetail winner3 = new WinReportsResponse.WinnerDetail("player1", 5, 4, 2, 80.0);
        WinReportsResponse.WinnerDetail winner4 = new WinReportsResponse.WinnerDetail("player3", 5, 3, 3, 60.0);
        WinReportsResponse.WinnerDetail winner5 = new WinReportsResponse.WinnerDetail("player4", 4, 1, 1, 25.0);

        List<WinReportsResponse.WinnerDetail> twoGuessWinnersList = Arrays.asList(winner3, winner4, winner5);

        // Set values directly using the constructor (no setters needed)
        winReports = new WinReportsResponse(
                20, // totalUsers
                4, // totalWinners
                Arrays.asList(guessCount1Winners, guessCount2Winners), // winsByGuessCount
                oneGuessWinnersList, // oneGuessWinners
                twoGuessWinnersList, // twoGuessWinners
                Arrays.asList() // threeGuessWinners (empty)
        );

        // Mock the service call
        when(reportService.getComprehensiveWinReports()).thenReturn(winReports);

        // Perform the test
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/win-reports")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(20))
                .andExpect(jsonPath("$.totalWinners").value(4))
                .andExpect(jsonPath("$.winsByGuessCount[0].guessCount").value(1))
                .andExpect(jsonPath("$.winsByGuessCount[0].winnersCount").value(2))
                .andExpect(jsonPath("$.oneGuessWinners[0].username").value("player1"))
                .andExpect(jsonPath("$.oneGuessWinners[0].totalWins").value(3))
                .andExpect(jsonPath("$.oneGuessWinners[0].winRate").value(75.0))
                .andExpect(jsonPath("$.twoGuessWinners[0].username").value("player1"))
                .andExpect(jsonPath("$.twoGuessWinners[1].username").value("player3"));
    }
}