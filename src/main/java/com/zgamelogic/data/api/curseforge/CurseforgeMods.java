package com.zgamelogic.data.api.curseforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseforgeMods {
    private List<CurseforgeMod> data;
}
