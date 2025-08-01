package com.oauth2.Util.Elasticsearch.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplementIngrComp implements Comparable<SupplementIngrComp>{
    private String name;
    private Double dose;
    private String unit;
    private boolean isMain;

    @Override
    public int compareTo(SupplementIngrComp o) {
        return this.dose.compareTo(o.getDose());
    }
}
