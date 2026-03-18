package com.lotto.repository;

import com.lotto.entity.LottoGame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LottoGameRepository extends JpaRepository<LottoGame, String> {
}
