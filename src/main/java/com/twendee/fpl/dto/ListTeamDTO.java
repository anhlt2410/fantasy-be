package com.twendee.fpl.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListTeamDTO {
    private List<TeamDTO> list = new ArrayList<>();
}
