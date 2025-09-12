package com.oauth2.Drug.Search.Dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientComp implements Comparable<IngredientComp> {
    private String name;
    private Float dose;
    private String backup;
    private boolean isMain;

    @Override
    public int compareTo(IngredientComp o) {
        return this.dose.compareTo(o.dose);
    }
}

