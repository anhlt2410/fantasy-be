package com.twendee.fpl.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListH2HDTO
{
    private int gameWeek;
    private List<H2HDTO> list;
}
