package com.twendee.fpl.service;

import com.twendee.fpl.dto.*;
import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.GameWeekResultRepository;
import com.twendee.fpl.repository.TeamRepository;
import com.twendee.fpl.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.twendee.fpl.constant.Constant.*;

@Slf4j
@Service
@Transactional
public class MainService {

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    GameWeekResultRepository gameWeekResultRepository;

    public FullGameWeekResultDTO getFullGameWeekResult(Integer gameWeek) {
        FullGameWeekResultDTO dto = new FullGameWeekResultDTO();
        List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByGameWeek(gameWeek);
        List<GameWeekResultDTO> gameWeekResultDTOS = gameWeekResults.stream().map(GameWeekResultDTO::new).sorted(Comparator.comparing(GameWeekResultDTO::getPosition)).collect(Collectors.toList());
        dto.getGameWeekResultDTOList().addAll(gameWeekResultDTOS);

        List<Long> doneList = new ArrayList<>();
        List<PairH2HDTO> h2HDTOList = new ArrayList<>();
        gameWeekResults.forEach(g -> {
            if (!doneList.contains(g.getTeam().getFplId())) {
                PairH2HDTO pairH2HDTO = new PairH2HDTO();
                pairH2HDTO.setTeam1fplId(g.getTeam().getFplId());
                pairH2HDTO.setTeam1Name(g.getTeam().getName());
                pairH2HDTO.setTeam1fplName(g.getTeam().getFplName());
                pairH2HDTO.setTeam1Point(g.getH2hPoint());
                doneList.add(g.getTeam().getFplId());

                if (g.getRival() != null) {
                    GameWeekResult rival = gameWeekResults.stream().filter(gameWeekResult -> gameWeekResult.getTeam().getFplId().equals(g.getRival().getFplId())).findAny().orElse(null);
                    if (rival != null) {
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

    public List<TopDTO> getTop() {
        List<TopDTO> tops = new ArrayList<>();
        List<GameWeekResult> topPoint = gameWeekResultRepository.findByPoint();
        tops.addAll(topPoint.stream().map(TopDTO::new).collect(Collectors.toList()));

        Team team = teamRepository.findFirstByOrderByMoneyAsc();
        tops.add(new TopDTO(team));

        return tops;
    }

    public String addTeam(ListTeamDTO dto) {
        List<Team> listToCreate = new ArrayList<>();
        try {
            dto.getList().forEach(t -> {
                Team team = new Team();
                team.setName(t.getName());
                team.setFplId(t.getFplId());
                team.setFplName(t.getFplName());

                listToCreate.add(team);
            });
            teamRepository.saveAll(listToCreate);
            return "SUCCESS";
        } catch (Exception e) {
            return "FAIL";
        }
    }

    public GameWeekH2HDTO getH2HFixturesByGameWeek(Long leagueId, Integer gameWeek) throws IOException {
        String uri = "https://fantasy.premierleague.com/api/leagues-h2h-matches/league/" + leagueId + "/?page=1&event=" + gameWeek;
        HttpResponse response = Utils.requestGet(uri);
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        JSONObject resultObject = new JSONObject(result);
        JSONArray arr = resultObject.getJSONArray("results");
        GameWeekH2HDTO gameWeekH2HDTO = new GameWeekH2HDTO();
        gameWeekH2HDTO.setGameWeek(gameWeek);
        arr.forEach(item -> {
            JSONObject pair = (JSONObject) item;
            Long rivalId1 = pair.isNull("entry_1_entry") ? null : pair.getLong("entry_1_entry");
            Long rivalId2 = pair.isNull("entry_2_entry") ? null : pair.getLong("entry_2_entry");
            H2HDTO h2HDTO = H2HDTO.builder()
                    .teamId1(rivalId1)
                    .teamId2(rivalId2)
                    .build();
            gameWeekH2HDTO.getH2HDTOList().add(h2HDTO);
        });

        return gameWeekH2HDTO;
    }

    public String updateFixture(GameWeekH2HDTO dtos) {
        List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByGameWeek(dtos.getGameWeek());

        if (gameWeekResults.isEmpty()) {
            gameWeekResults = createNewGameWeekForAll(dtos.getGameWeek());
        }


        List<H2HDTO> h2HDTOS = dtos.getH2HDTOList();

        try {
            for (GameWeekResult gameWeekResult : gameWeekResults) {
                Long teamFplId = gameWeekResult.getTeam().getFplId();
                Long rivalFplId = findRivalFromH2H(teamFplId, h2HDTOS);
                gameWeekResult.setRival(teamRepository.findByFplId(rivalFplId));

            }
        } catch (Exception e) {
            return "FAIL";
        }

        gameWeekResultRepository.saveAll(gameWeekResults);

        updateClassicOrderAndMoney(dtos.getGameWeek());
        return "SUCCESS - created data for " + gameWeekResults.size() + " game week";
    }

    private List<GameWeekResult> createNewGameWeekForAll(int gw) {
        List<Team> teams = teamRepository.findAll();

        List<GameWeekResult> gameWeekResults = new ArrayList<>();

        for (Team team : teams) {
            GameWeekResult gameWeekResult = new GameWeekResult();
            gameWeekResult.setGameWeek(gw);
            gameWeekResult.setTeam(team);

            gameWeekResults.add(gameWeekResult);
        }

        return gameWeekResults;
    }

    private Long findRivalFromH2H(Long teamFplId, List<H2HDTO> h2HDTOS) {
        H2HDTO rival = h2HDTOS.stream()
                .filter(item -> (item.getTeamId1().equals(teamFplId)))
                .findAny().orElse(null);

        if (rival != null) {
            return rival.getTeamId2();
        } else {
            rival = h2HDTOS.stream()
                    .filter(item -> (item.getTeamId2().equals(teamFplId)))
                    .findAny().orElse(null);

            return rival != null ? rival.getTeamId1() : null;
        }
    }

    public GameWeekResultDTO updateGameWeekResult(GameWeekPointDTO dto) throws IOException {

        Team team = teamRepository.findByFplId(dto.getTeamId());
        GameWeekResult gameWeekResult = gameWeekResultRepository.findByGameWeekAndTeam(dto.getGameWeek(), team);

        GameWeekH2HDTO gameWeekH2HDTO = getH2HFixturesByGameWeek(1657297L, dto.getGameWeek());
        if (gameWeekResult == null) {
            gameWeekResult = new GameWeekResult();
            gameWeekResult.setGameWeek(dto.getGameWeek());
            gameWeekResult.setTeam(team);

            //H2H rival
            GameWeekResult finalGameWeekResult = gameWeekResult;
            gameWeekH2HDTO.getH2HDTOList().forEach(h2HDTO -> {
                if (team.getFplId().equals(h2HDTO.getTeamId1())) {
                    finalGameWeekResult.setRival(h2HDTO.getTeamId2() == null ? null : teamRepository.findByFplId(h2HDTO.getTeamId2()));
                }
                if (team.getFplId().equals(h2HDTO.getTeamId2())) {
                    finalGameWeekResult.setRival(h2HDTO.getTeamId1() == null ? null : teamRepository.findByFplId(h2HDTO.getTeamId1()));
                }
            });
        }

        gameWeekResult.setTransfer(dto.getTransfer());
        gameWeekResult.setMinusPoints(dto.getMinusPoints());
        gameWeekResult.setPoint(dto.getPoint());

        Integer h2hPoint = dto.getPoint() + dto.getMinusPoints();
        gameWeekResult.setH2hPoint(h2hPoint);

        //TODO: check local Point (need to check bonus for top+bottom of previous gameweek)
        gameWeekResult.setLocalPoint(h2hPoint);

        if (dto.getMinusPoints() < 0) {
            gameWeekResult.setLocalPoint(h2hPoint + gameWeekResult.getNextFreeTransferBonus() * 4);
        }
        gameWeekResultRepository.save(gameWeekResult);
//        updateClassicOrderAndMoney(dto.getGameWeek());

        return new GameWeekResultDTO(gameWeekResult);
    }


    private void updateClassicOrderAndMoney(Integer gameWeek) {
        List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByGameWeek(gameWeek);
        List<GameWeekResult> activeGameWeekResults = gameWeekResults.stream().filter(g -> g.getTeam().getActive()).collect(Collectors.toList());
//        Integer leagueSize = gameWeekResults.size();
        activeGameWeekResults.sort(Comparator.comparing(GameWeekResult::getLocalPoint).reversed());

        List<GameWeekResult> nextGwBonusTeams = new ArrayList<>();
        int order = 1;
        GameWeekResult previous;
        for (GameWeekResult result : activeGameWeekResults) {
            result.setPosition(null);
        }
        for (GameWeekResult result : activeGameWeekResults) {
            previous = order == 1 ? result : activeGameWeekResults.get(order - 2);
            Integer rivalPoint = getRivalPoint(gameWeek, result.getRival(), gameWeekResults);
            if (rivalPoint != null && result.getH2hPoint() < rivalPoint) {
                result.setH2hMoney(UNIT_PRICE);
            } else {
                result.setH2hMoney(0d);
            }

            if (result.getLocalPoint().equals(previous.getLocalPoint()) && previous.getPosition() != null) {
                result.setPosition(previous.getPosition());
            } else {
                result.setPosition(order);
            }
//            GameWeekResult nextGameWeek = gameWeekResultRepository.findByGameWeekAndTeam(gameWeek + 1, result.getTeam());

//            if (nextGameWeek != null) {
//                if (result.getPosition() == 1 || result.getPosition().equals(leagueSize)) {
//                    nextGameWeek.setNextFreeTransferBonus(1);
//                } else {
//                    nextGameWeek.setNextFreeTransferBonus(0);
//                }
//                nextGwBonusTeams.add(nextGameWeek);
//
//            }
            order++;
        }

        activeGameWeekResults.sort(Comparator.comparing(GameWeekResult::getLocalPoint));
        activeGameWeekResults.forEach(gw -> gw.setMoney(0d));

        Map<Integer, CountMoney> moneyByPoint = new HashMap<>();

        Integer MAX_MONEY = (int) (activeGameWeekResults.size() / 2 * STEP);
        for (GameWeekResult gwResult : activeGameWeekResults) {
            int index = activeGameWeekResults.indexOf(gwResult);
            if (moneyByPoint.get(gwResult.getLocalPoint()) == null) {
                double money = MAX_MONEY - index * STEP;
                moneyByPoint.put(gwResult.getLocalPoint(), new CountMoney(1, money > 0 ? money : 0));
            } else {
                double currentMoney = MAX_MONEY - index * STEP;
                int countIncrease = moneyByPoint.get(gwResult.getLocalPoint()).count + 1;
                double money = moneyByPoint.get(gwResult.getLocalPoint()).money + (currentMoney > 0 ? currentMoney : 0);

                moneyByPoint.put(gwResult.getLocalPoint(), new CountMoney(countIncrease, money > 0 ? money : 0));

            }

            if (gwResult.getPosition() == 1) {
                gwResult.setVoucher(true);
            } else {
                gwResult.setVoucher(false);
            }
        }

        long numberOfTop1 = activeGameWeekResults.stream().filter(gwr -> gwr.getVoucher() != null && gwr.getVoucher()).count();

        for (GameWeekResult gwResult : activeGameWeekResults) {

            double money = 0 - Math.ceil((moneyByPoint.get(gwResult.getLocalPoint()).money / moneyByPoint.get(gwResult.getLocalPoint()).count) / 1000);
            gwResult.setMoney(money * 1000);

            if (gwResult.getPosition() == 1) {
                gwResult.setGameWeekWinnerMoney(VOUCHER/numberOfTop1);
            }
        }

        gameWeekResultRepository.saveAll(activeGameWeekResults);
//        gameWeekResultRepository.saveAll(nextGwBonusTeams);
    }

    private Integer getRivalPoint(Integer gameWeek, Team rival, List<GameWeekResult> gameWeekResults) {
        if (rival == null) {
            try {
                return getAverageByGameWeek(gameWeek);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            GameWeekResult rivalResult = gameWeekResults.stream()
                    .filter(item -> item.getTeam().getId().equals(rival.getId()))
                    .findAny().orElse(null);

            return rivalResult != null ? rivalResult.getH2hPoint() : null;
        }
    }


//    @Scheduled(cron = "0 0 * * * *")
    public void updateMainTable() {
        System.out.println("Start updateMainTable::" + new Date());
        Integer gameWeek = gameWeekResultRepository.getMaxGameWeek();
        updateClassicOrderAndMoney(gameWeek);


        List<Team> teams = teamRepository.findAllByOrderByPositionAsc();
        teams.forEach(team -> {
            List<GameWeekResult> gameWeekResults = gameWeekResultRepository.findByTeamAndGameWeekLessThan(team, gameWeek + 1);
            int sumPoint = gameWeekResults.stream().mapToInt(GameWeekResult::getLocalPoint).sum();
            double sumMoney = gameWeekResults.stream().mapToDouble(GameWeekResult::getMoney).sum();
            double sumH2hMoney = gameWeekResults.stream().mapToDouble(GameWeekResult::getH2hMoney).sum();
            team.setPoint(sumPoint);
            team.setMoney(sumMoney + sumH2hMoney);
            team.setVoucher(gameWeekResultRepository.countAllByTeamAndVoucherIsTrue(team));
            team.setGameWeekWinnerReward(gameWeekResults.stream().filter(gwr -> gwr.getGameWeekWinnerMoney() != null).mapToDouble(GameWeekResult::getGameWeekWinnerMoney).sum());
        });

        teams.sort(Comparator.comparing(Team::getPoint).reversed());
        int order = 1;

        for (Team team : teams) {
            team.setPosition(null);
        }

        Team previous;
        for (Team team : teams) {
            previous = order == 1 ? team : teams.get(order - 2);

            if (previous.getPosition() != null && team.getPoint().equals(previous.getPoint())) {
                team.setPosition(previous.getPosition());
            } else {
                team.setPosition(order);
            }
            order++;
        }
        teamRepository.saveAll(teams);
    }

    //Update points from FPL APIs

//    @Scheduled(cron = "0 0 * * * *")
    public void updateCurrentGameWeekAllTeamsPoint() throws IOException {
        updateAllTeamsPoint(null);
    }

    public Integer getCurrentGameWeek() throws IOException {
        String uri = "https://fantasy.premierleague.com/api/entry/164029/";
        HttpResponse gwResponse = Utils.requestGet(uri);

        HttpEntity gwEntity = gwResponse.getEntity();
        String gwResult = EntityUtils.toString(gwEntity, "UTF-8");
        JSONObject gwEntry = new JSONObject(gwResult);
        return gwEntry.getInt("current_event");
    }

    public void updateGWResultUntil(Integer gw) throws IOException {
        int currentGameWeek = gw == null ? getCurrentGameWeek() : gw;
        for (int i = 1; i <= currentGameWeek; i++) {
            updateAllTeamsPoint(i);
        }
    }
    public void updateAllTeamsPoint(Integer gameWeek) throws IOException {
        int currentGameWeek = gameWeek == null ? getCurrentGameWeek() : gameWeek;

        List<Team> teams = teamRepository.findAll();
        teams.forEach(team -> {
            try {
                updatePointFromAPIs(team.getFplId(), currentGameWeek);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        updateClassicOrderAndMoney(currentGameWeek);
    }
    public void updatePointFromAPIs(long teamId, int currentGameWeek) throws IOException {
        String uri = "https://fantasy.premierleague.com/api/entry/" + teamId + "/event/" + currentGameWeek + "/picks/";
        HttpResponse response = Utils.requestGet(uri);

        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        JSONObject jsonObject = new JSONObject(result);
        if (jsonObject.isNull("entry_history")) return;
        JSONObject entry = jsonObject.getJSONObject("entry_history");

        GameWeekPointDTO dto = new GameWeekPointDTO();
        dto.setTeamId(teamId);
        dto.setGameWeek(currentGameWeek);
        dto.setPoint(entry.getInt("points"));
        dto.setTransfer(entry.getInt("event_transfers"));
        dto.setMinusPoints(entry.getInt("event_transfers_cost")*(-1));

        updateGameWeekResult(dto);
    }

    public void addTeamsByLeagueId(String leagueId) throws IOException {
        String uri = "https://fantasy.premierleague.com/api/leagues-classic/" + leagueId + "/standings/?page_new_entries=1&page_standings=1&phase=1";
        HttpResponse response = Utils.requestGet(uri);

        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        JSONObject resultObject = new JSONObject(result);
        JSONObject newEntries = resultObject.getJSONObject("standings");
        JSONArray teamArr = newEntries.getJSONArray("results");
        List<Team> teams = new ArrayList<>();
        teamArr.forEach(team -> {
            JSONObject teamObj = (JSONObject) team;
            Long teamId = teamObj.getLong("entry");
            String fplName = teamObj.getString("entry_name");
            String userName = teamObj.getString("player_name");

            Team t = Team.builder()
                    .fplId(teamId)
                    .fplName(fplName)
                    .name(userName)
                    .build();
            teams.add(t);
        });
        teamRepository.saveAll(teams);
    }

    private Integer getAverageByGameWeek(Integer gameWeek) throws IOException {
        String uri = "https://fantasy.premierleague.com/api/leagues-h2h-matches/league/1657297/?page=1&event=" + gameWeek;
        HttpResponse response = Utils.requestGet(uri);

        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        JSONObject resultObject = new JSONObject(result);
        JSONArray resultsArray = resultObject.getJSONArray("results");
        AtomicReference<Integer> average = new AtomicReference<>(0);
        resultsArray.forEach(pair -> {
            JSONObject pairObj = (JSONObject) pair;

            if (pairObj.isNull("entry_1_entry")) {
                average.set(pairObj.getInt("entry_1_points"));
            }
            if (pairObj.isNull("entry_2_entry")) {
                average.set(pairObj.getInt("entry_2_points"));
            }
        });

        return average.get();
    }

    public RewardDTO getRewards() throws IOException {
        List<Team> top3LeaguePoint = teamRepository.findTop3ByOrderByPositionAsc();
        List<TopDTO> top3LeaguePointDTO = top3LeaguePoint.stream().map(TopDTO::new).collect(Collectors.toList());

        List<GameWeekResult> topGWPoint = gameWeekResultRepository.findByPoint();
        List<TopDTO> topGWPointDTO = topGWPoint.stream().map(TopDTO::new).collect(Collectors.toList());

        Team topDonate = teamRepository.findFirstByOrderByMoneyAsc();
        TopDTO topDonateDTO = new TopDTO(topDonate);
        topDonateDTO.setData(topDonate.getMoney().intValue());

        List<Team> gameWeekWinnerReward = teamRepository.findTop1Winners();
        List<TopDTO> gameWeekWinnerRewardDTO = gameWeekWinnerReward.stream().map(winner -> {
            TopDTO topDTO = new TopDTO(winner);
            topDTO.setData(winner.getGameWeekWinnerReward().intValue());
            return topDTO;
        }).collect(Collectors.toList());

        return RewardDTO.builder()
                .top3LeaguePoint(top3LeaguePointDTO)
                .topGWPoint(topGWPointDTO)
                .topDonate(topDonateDTO)
                .gameWeekWinnerReward(gameWeekWinnerRewardDTO)
                .top3H2H(getTop3H2H())
                .build();
    }

    private List<TopDTO> getTop3H2H() throws IOException {
        String uri = "https://fantasy.premierleague.com/api/leagues-h2h/1657297/standings/?page_new_entries=1&page_standings=1";
        HttpResponse response = Utils.requestGet(uri);

        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        JSONObject resultObject = new JSONObject(result);
        JSONObject standingsObject = resultObject.getJSONObject("standings");
        JSONArray resultsArray = standingsObject.getJSONArray("results");

        List<TopDTO> topH2H = new ArrayList<>();
        resultsArray.forEach(stand -> {
            JSONObject standObj = (JSONObject) stand;
            if (!standObj.isNull("rank") && standObj.getInt("rank") == 1) {
                topH2H.add(new TopDTO(1, standObj.getString("entry_name"), standObj.getString("player_name"), standObj.getInt("points_for")));
            }
            if (!standObj.isNull("rank") && standObj.getInt("rank") == 2) {
                topH2H.add(new TopDTO(2, standObj.getString("entry_name"), standObj.getString("player_name"), standObj.getInt("points_for")));
            }
            if (!standObj.isNull("rank") && standObj.getInt("rank") == 3) {
                topH2H.add(new TopDTO(3, standObj.getString("entry_name"), standObj.getString("player_name"), standObj.getInt("points_for")));
            }
        });

        return topH2H;
    }
}

class CountMoney {
    int count;
    double money;

    CountMoney(int count, double money) {
        this.count = count;
        this.money = money;
    }


}
