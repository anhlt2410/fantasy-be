package com.twendee.fpl.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GameWeekH2HDTO
{
    private int gameWeek;
    private List<H2HDTO> h2HDTOList = new ArrayList<>();
}
