package com.twendee.fpl.dto;

import com.twendee.fpl.model.GameWeekResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameWeekResultDTO
{
    private long id;
    private TeamDTO team;
    private int gameWeek;
    private int point;
    private int localPoint;
    private int transfer;
    private int position;
    private TeamDTO h2hRival;
    private double money;
    private double h2hMoney;
    private double h2hPoint;
    private double bonusTransfer = 0;

    public GameWeekResultDTO(GameWeekResult gameWeekResult)
    {
        this.id = gameWeekResult.getId();
        this.team = new TeamDTO(gameWeekResult.getTeam());
        this.gameWeek = gameWeekResult.getGameWeek();
        this.point = gameWeekResult.getPoint();
        this.transfer = gameWeekResult.getTransfer();
        this.position = gameWeekResult.getPosition();
        this.money = gameWeekResult.getMoney();
        this.h2hMoney = gameWeekResult.getH2hMoney();

        this.h2hPoint = gameWeekResult.getH2hPoint();
        this.bonusTransfer = gameWeekResult.getNextFreeTransferBonus();
        this.localPoint = gameWeekResult.getLocalPoint();


//        if (gameWeekResult.getRival() != null)
//            this.h2hRival = new TeamDTO(gameWeekResult.getRival());
    }
}
