package com.twendee.fpl.controller;

import com.twendee.fpl.constant.Constant;
import com.twendee.fpl.dto.*;
import com.twendee.fpl.model.GameWeekResult;
import com.twendee.fpl.model.Team;
import com.twendee.fpl.repository.GameWeekResultRepository;
import com.twendee.fpl.repository.TeamRepository;
import com.twendee.fpl.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired
    TeamRepository teamRepository;
    @Autowired
    GameWeekResultRepository gameWeekResultRepository;
    @Autowired
    MainService mainService;

    @GetMapping("/league-table")
    public ResponseEntity<List<Team>> getAll() {
        List<Team> teams = teamRepository.findAllByOrderByPositionAsc();
        if (teams.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Team>>(teams, HttpStatus.OK);
    }

    @PostMapping("/add-team")
    public ResponseEntity<String> addTeam(@RequestBody ListTeamDTO dto) {
        String result = mainService.addTeam(dto);
        if (result.equals("FAIL")){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>(result, HttpStatus.CREATED);
    }

    @GetMapping("/get-team-info/{id}")
    public ResponseEntity<Team> getOne(@PathVariable(name = "id") Long id) {
        Team team = teamRepository.findById(id).orElse(null);
        if (team == null) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<Team>(team, HttpStatus.OK);
    }

    @GetMapping("/get-game-week-info-by-team")
    public ResponseEntity<GameWeekResultDTO> getGameWeekInfoByTeam(@RequestParam(name = "teamId") Long teamId,
                                                                   @RequestParam(name = "gameWeek") Integer gameWeek) {
        Team team = teamRepository.findById(teamId).orElse(null);
        GameWeekResult gameWeekResult = gameWeekResultRepository.findByGameWeekAndTeam(gameWeek, team);
        if (gameWeekResult == null) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<GameWeekResultDTO>(new GameWeekResultDTO(gameWeekResult), HttpStatus.OK);
    }

    @GetMapping("/get-tops")
    public ResponseEntity<List<TopDTO>> getTops() {
        return new ResponseEntity<>(mainService.getTop(), HttpStatus.OK);
    }

    @GetMapping("/get-game-week-info")
    public ResponseEntity<FullGameWeekResultDTO> getGameWeekInfo(@RequestParam(name = "gameWeek") Integer gameWeek) {
        FullGameWeekResultDTO dto = mainService.getFullGameWeekResult(gameWeek);
        return new ResponseEntity<FullGameWeekResultDTO>(dto, HttpStatus.OK);
    }

    @PostMapping("/update-league-result")
    public ResponseEntity<GameWeekResultDTO> updateLeagueResult(@RequestBody GameWeekPointDTO dto) throws IOException {
        GameWeekResultDTO gameWeekResultDTO = mainService.updateGameWeekResult(dto);
        return new ResponseEntity<GameWeekResultDTO>(gameWeekResultDTO, HttpStatus.OK);
    }

    @PostMapping("/update-fixture")
    public ResponseEntity<String> updateFixture(@RequestBody GameWeekH2HDTO dtos) {
        String result = mainService.updateFixture(dtos);
        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    @PostMapping("/update-main-table")
    public ResponseEntity<String> updateMainTable() {
        mainService.updateMainTable();
        return new ResponseEntity<String>("OK", HttpStatus.OK);
    }

    @PostMapping("/update-all-team-points")
    public ResponseEntity<String> updateAllTeamPoint(@RequestParam("gameWeek") Integer gameWeek) throws IOException {
        mainService.updateAllTeamsPoint(gameWeek);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }



    @GetMapping("/currentGameweek")
    public ResponseEntity<Integer> currentGameweek() throws IOException {
        return new ResponseEntity<Integer>(mainService.getCurrentGameWeek(), HttpStatus.OK);
    }


    @PostMapping("/currentGameweek")
    public ResponseEntity<Integer> updateCurrentGameweek(@RequestParam(name = "gameWeek") Integer currentGameWeek) {
        Constant.CURRENT_GW = currentGameWeek;
        return new ResponseEntity<Integer>(Constant.CURRENT_GW, HttpStatus.OK);
    }

    @GetMapping("/add-teams-by-league-id/{id}")
    public ResponseEntity<?> addTeams(@PathVariable(name = "id") String id) throws IOException {
        mainService.addTeamsByLeagueId(id);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/get-rewards")
    public ResponseEntity<?> getRewards() throws IOException {
        return ResponseEntity.ok(mainService.getRewards());
    }

}
