package com.twendee.fpl.service;

import com.twendee.fpl.dto.*;
import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.GameWeekResultRepository;
import com.twendee.fpl.repository.TeamRepository;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.twendee.fpl.constant.Constant.*;

@Slf4j
@Service
@Transactional
public class MainService
{

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    GameWeekResultRepository gameWeekResultRepository;

    public FullGameWeekResultDTO getFullGameWeekResult(Integer gameWeek)
    {
        FullGameWeekResultDTO dto = new FullGameWeekResultDTO();
        List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByGameWeek(gameWeek);
        List<GameWeekResultDTO> gameWeekResultDTOS = gameWeekResults.stream().map(GameWeekResultDTO::new).sorted(Comparator.comparing(GameWeekResultDTO::getPosition)).collect(Collectors.toList());
        dto.getGameWeekResultDTOList().addAll(gameWeekResultDTOS);

        List<Long> doneList = new ArrayList<>();
        List<PairH2HDTO> h2HDTOList = new ArrayList<>();
        gameWeekResults.forEach(g -> {
            if (!doneList.contains(g.getTeam().getFplId()))
            {
                PairH2HDTO pairH2HDTO = new PairH2HDTO();
                pairH2HDTO.setTeam1fplId(g.getTeam().getFplId());
                pairH2HDTO.setTeam1Name(g.getTeam().getName());
                pairH2HDTO.setTeam1fplName(g.getTeam().getFplName());
                pairH2HDTO.setTeam1Point(g.getH2hPoint());
                doneList.add(g.getTeam().getFplId());

                if (g.getRival() != null)
                {
                    GameWeekResult rival = gameWeekResults.stream().filter(gameWeekResult -> gameWeekResult.getTeam().getFplId().equals(g.getRival().getFplId())).findAny().orElse(null);
                    if (rival != null)
                    {
                        pairH2HDTO.setTeam2fplId(rival.getTeam().getFplId());
                        pairH2HDTO.setTeam2Name(rival.getTeam().getFplName());
                        pairH2HDTO.setTeam2fplName(rival.getTeam().getFplName());
                        pairH2HDTO.setTeam2Point(rival.getH2hPoint());
                        doneList.add(rival.getTeam().getFplId());
                    }
                }
                h2HDTOList.add(pairH2HDTO);
            }
        });

        dto.getH2HDTOList().addAll(h2HDTOList);

        return dto;
    }

    public String addTeam(ListTeamDTO dto)
    {
        List<Team> listToCreate = new ArrayList<>();
        try
        {
            dto.getList().forEach(t -> {
                Team team = new Team();
                team.setName(t.getName());
                team.setFplId(t.getFplId());
                team.setFplName(t.getFplName());

                listToCreate.add(team);
            });
            teamRepository.saveAll(listToCreate);
            return "SUCCESS";
        }
        catch (Exception e)
        {
            return "FAIL";
        }
    }

    public String updateFixture(ListH2HDTO dtos)
    {
        List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByGameWeek(dtos.getGameWeek());

        if (gameWeekResults.isEmpty())
        {
            gameWeekResults = createNewGameWeekForAll(dtos.getGameWeek());
        }


        List<H2HDTO> h2HDTOS = dtos.getList();

        try
        {
            for (GameWeekResult gameWeekResult : gameWeekResults)
            {
                Long teamFplId = gameWeekResult.getTeam().getFplId();
                Long rivalFplId = findRivalFromH2H(teamFplId, h2HDTOS);
                gameWeekResult.setRival(teamRepository.findByFplId(rivalFplId));

            }
        }
        catch (Exception e)
        {
            return "FAIL";
        }

        gameWeekResultRepository.saveAll(gameWeekResults);

        updateClassicOrderAndMoney(dtos.getGameWeek());
        return "SUCCESS - created data for " + gameWeekResults.size() + " game week";
    }

    private List<GameWeekResult> createNewGameWeekForAll(int gw)
    {
        List<Team> teams = teamRepository.findAll();

        List<GameWeekResult> gameWeekResults = new ArrayList<>();

        for (Team team : teams)
        {
            GameWeekResult gameWeekResult = new GameWeekResult();
            gameWeekResult.setGameWeek(gw);
            gameWeekResult.setTeam(team);

            gameWeekResults.add(gameWeekResult);
        }

        return gameWeekResults;
    }

    private Long findRivalFromH2H(Long teamFplId, List<H2HDTO> h2HDTOS)
    {
        H2HDTO rival = h2HDTOS.stream()
                .filter(item -> (item.getTeam1().equals(teamFplId)))
                .findAny().orElse(null);

        if (rival != null)
        {
            return rival.getTeam2();
        }
        else
        {
            rival = h2HDTOS.stream()
                    .filter(item -> (item.getTeam2().equals(teamFplId)))
                    .findAny().orElse(null);

            return rival != null ? rival.getTeam1() : null;
        }
    }

    public GameWeekResultDTO updateGameWeekResult(GameWeekPointDTO dto)
    {

        Team team = teamRepository.findByFplId(dto.getTeamId());
        GameWeekResult gameWeekResult = gameWeekResultRepository.findByGameWeekAndTeam(dto.getGameWeek(), team);

        if (gameWeekResult == null)
        {
            gameWeekResult = new GameWeekResult();
            gameWeekResult.setGameWeek(dto.getGameWeek());
            gameWeekResult.setTeam(team);
        }

        gameWeekResult.setTransfer(dto.getTransfer());
        gameWeekResult.setMinusPoints(dto.getMinusPoints());
        gameWeekResult.setPoint(dto.getPoint());

        Integer h2hPoint = dto.getPoint() + dto.getMinusPoints();
        gameWeekResult.setH2hPoint(h2hPoint);

        //TODO: check local Point (need to check bonus for top+bottom of previous gameweek)
        gameWeekResult.setLocalPoint(h2hPoint);

        if (dto.getMinusPoints() < 0)
        {
            gameWeekResult.setLocalPoint(h2hPoint + gameWeekResult.getNextFreeTransferBonus() * 4);
        }
        gameWeekResultRepository.save(gameWeekResult);
        updateClassicOrderAndMoney(dto.getGameWeek());

        return new GameWeekResultDTO(gameWeekResult);
    }


    private void updateClassicOrderAndMoney(Integer gameWeek)
    {
        List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByGameWeek(gameWeek);
        Integer leagueSize = gameWeekResults.size();
        gameWeekResults.sort(Comparator.comparing(GameWeekResult::getLocalPoint).reversed());

        List<GameWeekResult> nextGwBonusTeams = new ArrayList<>();
        int order = 1;
        GameWeekResult previous;
        for (GameWeekResult result : gameWeekResults)
        {
            result.setPosition(null);
        }
        for (GameWeekResult result : gameWeekResults)
        {
            previous = order == 1 ? result : gameWeekResults.get(order - 2);
//            if (order <= gameWeekResults.size() / 2)
//            {
//                result.setMoney(0D);
//            }
//            else
//            {
//                //TODO: HANDLE CASE EQUAL POINTS
//                result.setMoney((order - gameWeekResults.size() / 2) * UNIT_PRICE);
//            }

            Integer rivalPoint = getRivalPoint(result.getRival(), gameWeekResults);
            if (rivalPoint != null && result.getH2hPoint() < rivalPoint)
            {
                result.setH2hMoney(UNIT_PRICE);
            }
            else
            {
                result.setH2hMoney(0d);
            }


            if (result.getLocalPoint().equals(previous.getLocalPoint()) && previous.getPosition() != null)
            {
                result.setPosition(previous.getPosition());
            }
            else
            {
                result.setPosition(order);
            }
            GameWeekResult nextGameWeek = gameWeekResultRepository.findByGameWeekAndTeam(gameWeek + 1, result.getTeam());

            if(nextGameWeek != null ) {
                if (result.getPosition() == 1 || result.getPosition().equals(leagueSize)) {
                    nextGameWeek.setNextFreeTransferBonus(1);
                } else {
                    nextGameWeek.setNextFreeTransferBonus(0);
                }
                nextGwBonusTeams.add(nextGameWeek);

            }

            order++;

        }

        gameWeekResults.sort(Comparator.comparing(GameWeekResult::getLocalPoint));
        gameWeekResults.forEach(gw -> gw.setMoney(0d));

        Map<Integer, CountMoney> moneyByPoint = new HashMap<>();

        Integer MAX_MONEY = (int) (gameWeekResults.size() / 2 * STEP);
        for (GameWeekResult gwResult : gameWeekResults)
        {
            int index = gameWeekResults.indexOf(gwResult);
            if (moneyByPoint.get(gwResult.getLocalPoint()) == null)
            {
                double money = MAX_MONEY - index * STEP;
                moneyByPoint.put(gwResult.getLocalPoint(), new CountMoney(1, money > 0 ? money : 0));
            }
            else
            {
                double currentMoney = MAX_MONEY - index * STEP;
                int countIncreas = moneyByPoint.get(gwResult.getLocalPoint()).count + 1;
                double money = moneyByPoint.get(gwResult.getLocalPoint()).money + (currentMoney > 0 ? currentMoney : 0);

                moneyByPoint.put(gwResult.getLocalPoint(), new CountMoney(countIncreas, money > 0 ? money : 0));

            }
        }

        for (GameWeekResult gwResult : gameWeekResults)
        {
            double money = 0 - Math.ceil((1.0 * moneyByPoint.get(gwResult.getLocalPoint()).money / moneyByPoint.get(gwResult.getLocalPoint()).count) / 1000);
            gwResult.setMoney(money * 1000);

            //TODO: BONUS FOR TWENDEE LEAGUE
            if (gwResult.getPosition() == 1)
            {
                gwResult.setMoney(TOP_BONUS_PRICE);
            }
        }

        gameWeekResultRepository.saveAll(gameWeekResults);
        gameWeekResultRepository.saveAll(nextGwBonusTeams);
    }

    private Integer getRivalPoint(Team rival, List<GameWeekResult> gameWeekResults)
    {
        if (rival == null)
        {
            return null;
        }

        else
        {
            GameWeekResult rivalResult = gameWeekResults.stream()
                    .filter(item -> item.getTeam().getId().equals(rival.getId()))
                    .findAny().orElse(null);

            return rivalResult != null ? rivalResult.getH2hPoint() : null;
        }
    }


    @Scheduled(cron = "0 0/10 * * * *")
    public void updateMainTable()
    {
        System.out.println("Start updateMainTable::" + new Date());
        Integer gameWeek = CURRENT_GW;
        updateClassicOrderAndMoney(gameWeek);


        List<Team> teams = teamRepository.findAllByOrderByPositionAsc();
        teams.forEach(team -> {
            List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByTeamAndGameWeekLessThan(team, gameWeek + 1);
            int sumPoint = gameWeekResults.stream().mapToInt(GameWeekResult::getPoint).sum();
            double sumMoney = gameWeekResults.stream().mapToDouble(GameWeekResult::getMoney).sum();
            team.setPoint(sumPoint);
            team.setMoney(sumMoney);
//            team.setH2hMoney(sumH2HMoney);
        });

        teams.sort(Comparator.comparing(Team::getPoint).reversed());
        int order = 1;

        for (Team team : teams)
        {
            team.setPosition(null);
        }

        Team previous;
        for (Team team : teams)
        {
            previous = order == 1 ? team : teams.get(order - 2);

            if (previous.getPosition() != null && team.getPoint().equals(previous.getPoint()))
            {
                team.setPosition(previous.getPosition());
            }
            else
            {
                team.setPosition(order);
            }
            order++;
        }
        teamRepository.saveAll(teams);
    }

}

class CountMoney
{
    int count;
    double money;

    CountMoney(int count, double money)
    {
        this.count = count;
        this.money = money;
    }


}
