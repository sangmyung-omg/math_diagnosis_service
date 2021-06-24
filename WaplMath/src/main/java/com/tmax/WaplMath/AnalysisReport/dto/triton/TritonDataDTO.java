package com.tmax.WaplMath.AnalysisReport.dto.triton;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TritonDataDTO {
    private String name;
    private List<Integer> shape;
    private String datatype;
    private List<String> data;
}
