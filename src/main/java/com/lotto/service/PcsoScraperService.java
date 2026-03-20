package com.lotto.service;

import com.lotto.entity.LottoGame;
import com.lotto.entity.OfficialResult;
import com.lotto.repository.LottoGameRepository;
import com.lotto.repository.OfficialResultRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PcsoScraperService {

    private static final String PCSO_URL = "https://www.pcso.gov.ph/searchlottoresult.aspx";
    private final OfficialResultRepository resultRepo;
    private final LottoGameRepository gameRepo;

    public PcsoScraperService(OfficialResultRepository resultRepo, LottoGameRepository gameRepo) {
        this.resultRepo = resultRepo;
        this.gameRepo = gameRepo;
    }

    /**
     * Map PCSO website game names to our database IDs
     */
    private String mapGameNameToId(String pcsoName) {
        String name = pcsoName.toLowerCase();
        if (name.contains("ultra") && name.contains("6/58")) return "ultra-658";
        if (name.contains("grand") && name.contains("6/55")) return "grand-655";
        if (name.contains("super") && name.contains("6/49")) return "super-649";
        if (name.contains("mega") && name.contains("6/45")) return "mega-645";
        if (name.contains("lotto") && name.contains("6/42")) return "lotto-642";
        if (name.contains("6d lotto")) return "6digit";
        if (name.contains("4d lotto")) return "4digit";
        if (name.contains("3d lotto")) return "3d-swertres";
        if (name.contains("2d lotto")) return "2d-ez2";
        return null;
    }

    /**
     * Normalize draw date from PCSO format (M/d/yyyy) to our database format (yyyy-MM-dd)
     */
    private String normalizeDrawDate(String pcsoDate) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            LocalDate date = LocalDate.parse(pcsoDate, inputFormatter);
            return date.toString(); // yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fetch and update results from PCSO
     */
    @Transactional
    public Map<String, Object> updateResultsFromPcso() {
        int updatedCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            // In a real scenario, you might need to handle the ASP.NET VIEWSTATE and EVENTVALIDATION 
            // if you wanted to POST search criteria. 
            // For now, we'll try to scrape the default landing page results which usually show recent draws.
            Document doc = Jsoup.connect(PCSO_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .get();

            // The table usually has a specific structure. Based on the provided content:
            // LOTTO GAME | COMBINATIONS | DRAW DATE | JACKPOT | WINNERS
            Elements rows = doc.select("table tr");
            
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 3) continue;

                String gameName = cols.get(0).text().trim();
                String combinations = cols.get(1).text().trim();
                String drawDateRaw = cols.get(2).text().trim();

                String gameId = mapGameNameToId(gameName);
                String drawDateKey = normalizeDrawDate(drawDateRaw);

                if (gameId != null && drawDateKey != null && !combinations.isEmpty()) {
                    // Check if result already exists
                    Optional<OfficialResult> existing = resultRepo.findAll().stream()
                            .filter(r -> r.getGameId().equals(gameId) && r.getDrawDateKey().equals(drawDateKey))
                            .findFirst();

                    if (existing.isEmpty()) {
                        OfficialResult result = new OfficialResult();
                        result.setGameId(gameId);
                        result.setDrawDateKey(drawDateKey);
                        // Clean up combinations (PCSO uses hyphen usually, we use comma)
                        String formattedNumbers = combinations.replace("-", ",").replaceAll("\\s+", "");
                        result.setNumbers(formattedNumbers);
                        resultRepo.save(result);
                        updatedCount++;
                        
                        // Also update jackpot if available
                        if (cols.size() >= 4) {
                            String jackpotStr = cols.get(3).text().replaceAll("[^0-9.]", "");
                            try {
                                long jackpot = (long) Double.parseDouble(jackpotStr);
                                Optional<LottoGame> gameOpt = gameRepo.findById(gameId);
                                if (gameOpt.isPresent()) {
                                    LottoGame game = gameOpt.get();
                                    game.setJackpot(jackpot);
                                    gameRepo.save(game);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

        } catch (Exception e) {
            errors.add("Failed to scrape PCSO: " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("updatedCount", updatedCount);
        response.put("errors", errors);
        response.put("status", errors.isEmpty() ? "success" : "partial_success");
        return response;
    }
}
