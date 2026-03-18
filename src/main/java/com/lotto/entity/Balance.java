package com.lotto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "balances")
public class Balance {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount = new BigDecimal("5000.00");

    public Balance() {}

    public Balance(Long userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
