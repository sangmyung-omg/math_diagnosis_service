package com.tmax.WaplMath.Recommend.dto.mastery;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TritonMasteryDTO {
    private Map<Integer, Float> mastery;
    private String embedding;
}
