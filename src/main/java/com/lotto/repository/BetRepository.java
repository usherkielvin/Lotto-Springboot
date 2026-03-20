package com.lotto.repository;

import com.lotto.entity.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BetRepository extends JpaRepository<Bet, String> {
    List<Bet> findByUserIdOrderByPlacedAtDesc(Long userId);
    List<Bet> findByUserIdAndStatusOrderByPlacedAtDesc(Long userId, String status);
    List<Bet> findByUserIdAndStatusNotOrderByPlacedAtDesc(Long userId, String status);
    long countByGameIdAndDrawDateKeyAndDrawTimeAndStatus(String gameId, String drawDateKey, String drawTime, String status);
    List<Bet> findByGameIdAndDrawDateKeyAndDrawTimeAndStatus(String gameId, String drawDateKey, String drawTime, String status);

    /** Case-insensitive draw time match — catches "9:00 PM" vs "9:00 pm" mismatches */
    @Query("SELECT b FROM Bet b WHERE b.gameId = :gameId AND b.drawDateKey = :drawDateKey AND UPPER(b.drawTime) = UPPER(:drawTime) AND b.status = 'pending'")
    List<Bet> findPendingByGameDateTimeIgnoreCase(@Param("gameId") String gameId,
                                                  @Param("drawDateKey") String drawDateKey,
                                                  @Param("drawTime") String drawTime);
}
