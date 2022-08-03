package com.twendee.fpl.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListListH2HDTO {
    List<ListH2HDTO> list = new ArrayList<>();
}
