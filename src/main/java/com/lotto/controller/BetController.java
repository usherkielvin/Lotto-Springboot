package com.lotto.controller;

import com.lotto.entity.Balance;
import com.lotto.repository.BalanceRepository;
import com.lotto.service.BetService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bets")
public class BetController {

    private final BetService betService;
    private final BalanceRepository balanceRepo;

    public BetController(BetService betService, BalanceRepository balanceRepo) {
        this.betService = betService;
        this.balanceRepo = balanceRepo;
    }

    /** Place a new bet */
    @PostMapping
    public ResponseEntity<?> placeBet(@RequestHeader("X-User-Id") @NonNull Long userId,
                                       @RequestBody Map<String, Object> body) {
        try {
            String gameId = (String) body.get("gameId");
            if (gameId == null) throw new RuntimeException("gameId is required.");
            @SuppressWarnings("unchecked")
            List<Integer> numbers = (List<Integer>) body.get("numbers");
            BigDecimal stake = new BigDecimal(body.get("stake").toString());
            return ResponseEntity.ok(betService.placeBet(userId, gameId, numbers, stake));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Active (pending) tickets */
    @GetMapping
    public ResponseEntity<?> getActiveBets(@RequestHeader("X-User-Id") @NonNull Long userId) {
        try {
            return ResponseEntity.ok(betService.getActiveBets(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Settled bet history */
    @GetMapping("/history")
    public ResponseEntity<?> getBetHistory(@RequestHeader("X-User-Id") @NonNull Long userId) {
        try {
            return ResponseEntity.ok(betService.getBetHistory(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Current balance */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("X-User-Id") @NonNull Long userId) {
        Balance balance = balanceRepo.findById(userId).orElse(null);
        if (balance == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("balance", balance.getAmount()));
    }
}
