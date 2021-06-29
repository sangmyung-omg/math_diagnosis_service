package com.tmax.WaplMath.AnalysisReport.dto.triton;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TritonRequestDTO {
    private String id;
    private List<TritonDataDTO> inputs;
    private List<TritonDataDTO> outputs;
}
