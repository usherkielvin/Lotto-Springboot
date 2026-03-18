package com.lotto.controller;

import com.lotto.entity.LottoGame;
import com.lotto.repository.LottoGameRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final LottoGameRepository gameRepo;

    public GameController(LottoGameRepository gameRepo) {
        this.gameRepo = gameRepo;
    }

    @GetMapping
    public List<LottoGame> listGames() {
        return gameRepo.findAll();
    }
}
