package com.lotto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lotto_games")
public class LottoGame {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_number", nullable = false)
    private int maxNumber;

    @Column(name = "draw_time", nullable = false)
    private String drawTime;

    public LottoGame() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMaxNumber() { return maxNumber; }
    public void setMaxNumber(int maxNumber) { this.maxNumber = maxNumber; }
    public String getDrawTime() { return drawTime; }
    public void setDrawTime(String drawTime) { this.drawTime = drawTime; }
}
