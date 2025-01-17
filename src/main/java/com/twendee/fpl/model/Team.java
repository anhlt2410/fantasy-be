package com.twendee.fpl.model;

import lombok.*;

import javax.persistence.Entity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class Team extends BaseEntity{
    private String name;
    private String fplName;
    private Long fplId;
    private Integer point = 0;
    private Integer position = 0;
    private Double money = 0D;
    private Double paid = 0D;
    private Double h2hMoney = 0D;
    private Integer voucher;
    private Double gameWeekWinnerReward = 0D;
    private Boolean active;

    public Double getMoney() {
        return money - (paid == null ? 0 : paid);
    }
}
