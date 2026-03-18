package com.lotto.repository;

import com.lotto.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BetRepository extends JpaRepository<Bet, String> {
    List<Bet> findByUserIdOrderByPlacedAtDesc(Long userId);
    List<Bet> findByUserIdAndStatusOrderByPlacedAtDesc(Long userId, String status);
    List<Bet> findByUserIdAndStatusNotOrderByPlacedAtDesc(Long userId, String status);
}
