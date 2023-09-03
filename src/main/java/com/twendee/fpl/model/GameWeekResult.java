package com.twendee.fpl.model;

import com.twendee.fpl.config.BooleanYnConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameWeekResult extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer gameWeek;
    private Integer point = 0;
    private Integer h2hPoint = 0;
    private Integer localPoint = 0;
    private Integer transfer = 0;
    private Integer minusPoints = 0;
    private Integer position = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rival_id")
    private Team rival;

    private Double money = 0D;
    private Double h2hMoney = 0D;
    private Integer nextFreeTransferBonus = 0;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean voucher;

    private Double gameWeekWinnerMoney = 0D;
}
